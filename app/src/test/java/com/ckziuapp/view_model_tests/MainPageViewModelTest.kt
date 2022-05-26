package com.ckziuapp.view_model_tests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.model.Failure
import com.ckziu_app.model.InProgress
import com.ckziu_app.model.MainPageInfo
import com.ckziu_app.model.Success
import com.ckziu_app.ui.viewmodels.MainPageViewModel
import com.ckziuapp.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainPageViewModelTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MainPageViewModel

    @Mock
    private lateinit var repo: MainPageRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = MainPageViewModel(
            repo,
            mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun updateMainPageInfo_returnsSuccess_whenMainPageInfoGetterRerunsSuccess() = runTest {
        val emptyMainPageInfo = MainPageInfo.emptyMainPageInfo()

        whenever(repo.flowOfMainPageInfo()).thenReturn(
            flow {
                emit(InProgress())
                delay(2000)
                emit(Success(emptyMainPageInfo))
            }
        )

        viewModel.collectMainPageInfo()

        val progressValue = viewModel.mainPageInfo.value
        assertThat(progressValue).isInstanceOf(InProgress::class.java)

        advanceUntilIdle()

        val successValue = viewModel.mainPageInfo.value
        assertThat(successValue).isInstanceOf(Success::class.java)
        assertThat((successValue as Success).resultValue).isEqualTo(emptyMainPageInfo)
    }

    @Test
    fun updateMainPageInfo_returnsFailure_whenMainPageInfoGetterRerunsFailure() = runTest {
        whenever(repo.flowOfMainPageInfo()).thenReturn(
            flow {
                emit(InProgress())
                delay(2000)
                emit(Failure<MainPageInfo>())
            }
        )

        viewModel.collectMainPageInfo()

        val progressResult = viewModel.mainPageInfo.value
        assertThat(progressResult).isInstanceOf(InProgress::class.java)

        advanceUntilIdle()

        val failureValue = viewModel.mainPageInfo.value
        assertThat(failureValue).isInstanceOf(Failure::class.java)
    }
}