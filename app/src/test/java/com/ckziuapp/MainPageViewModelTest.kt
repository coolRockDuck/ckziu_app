package com.ckziuapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ckziu_app.model.InProgress
import com.ckziu_app.model.Result
import com.ckziu_app.model.Success
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.model.MainPageInfo
import com.ckziu_app.ui.viewmodels.MainPageViewModel
import com.ckziu_app.ui.viewmodels.factories.MainPageViewModelFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
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

    @Before
    fun setUp() {

        MockitoAnnotations.openMocks(this)
        viewModel = MainPageViewModelFactory(
            repo,
            testCoroutineDispatcher
        ).create(MainPageViewModel::class.java)

        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @After
    fun tearDown() {
        testCoroutineDispatcher.cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

    @Test
    fun updateMainPageInfo_Test() = runBlocking {

        val mainPageInfo = MainPageInfo.emptyMainPageInfo()
        whenever(repo.flowOfMainPageInfo()).thenReturn(flow {
            emit(InProgress())
            emit(Success(mainPageInfo))
        })

        viewModel.mainPageInfo.observeForever(observer)

        viewModel.collectMainPageInfo()

        val argCollector = ArgumentCaptor.forClass(viewModel.mainPageInfo.value!!.javaClass)
        verify(observer, times(2)).onChanged(capture(argCollector))

        assertThat(argCollector.allValues.first()).isInstanceOf(InProgress::class.java)
        assertThat(argCollector.allValues[1]).isInstanceOf(Success::class.java)
        assertThat((argCollector.allValues[1] as Success).resultValue).isEqualTo(mainPageInfo)
    }
}