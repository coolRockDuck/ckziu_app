package com.ckziuapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.model.*
import com.ckziu_app.ui.viewmodels.MainPageViewModel
import com.ckziu_app.ui.viewmodels.factories.MainPageViewModelFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainPageViewModelTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var viewModel: MainPageViewModel

    @Mock
    private lateinit var repo: MainPageRepository

    @Mock
    private lateinit var observer: Observer<Result<MainPageInfo>>

    @Captor
    private lateinit var argCollector: ArgumentCaptor<Result<MainPageInfo>>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testCoroutineDispatcher)
        viewModel = MainPageViewModelFactory(
            repo,
            testCoroutineDispatcher
        ).create(MainPageViewModel::class.java).apply {
            mainPageInfo.observeForever(observer)
        }
    }


    @After
    fun tearDown() {
        testCoroutineDispatcher.cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

    @Test
    fun updateMainPageInfo_returnsSuccess_whenMainPageInfoGetterRerunsSuccess() = runBlockingTest {

        val mainPageInfo = MainPageInfo.emptyMainPageInfo()

        whenever(repo.flowOfMainPageInfo()).thenReturn(
            flow {
                emit(InProgress())
                delay(2000)
                emit(Success(mainPageInfo))
            }
        )

        viewModel.collectMainPageInfo()
        verify(observer, times(2)).onChanged(capture(argCollector))
        assertThat(argCollector.value).isInstanceOf(InProgress::class.java)

        testCoroutineDispatcher.advanceTimeBy(3000)

        verify(observer, times(3)).onChanged(capture(argCollector))

        assertThat(argCollector.value).isInstanceOf(Success::class.java)
        assertThat((argCollector.value as Success).resultValue).isEqualTo(mainPageInfo)
    }

    @Test
    fun updateMainPageInfo_returnsFailure_whenMainPageInfoGetterRerunsFailure() = runBlockingTest {
        whenever(repo.flowOfMainPageInfo()).thenReturn(
            flow {
                emit(InProgress())
                delay(2000)
                emit(Failure<MainPageInfo>())
            }
        )

        viewModel.collectMainPageInfo()
        verify(observer, times(2)).onChanged(capture(argCollector))
        assertThat(argCollector.value).isInstanceOf(InProgress::class.java)

        testCoroutineDispatcher.advanceTimeBy(2000)
        verify(observer, times(3)).onChanged(capture(argCollector))
        assertThat(argCollector.value).isInstanceOf(Failure::class.java)
    }
}