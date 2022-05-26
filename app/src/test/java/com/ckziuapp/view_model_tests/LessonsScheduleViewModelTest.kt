package com.ckziuapp.view_model_tests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ckziu_app.data.repositories.LessonsScheduleRepository
import com.ckziu_app.model.*
import com.ckziu_app.ui.viewmodels.LessonsScheduleViewModel
import com.ckziuapp.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LessonsScheduleViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LessonsScheduleViewModel

    @Mock
    private lateinit var repo: LessonsScheduleRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = LessonsScheduleViewModel(
            repo,
            mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun updateTargets_shouldUpdateTargets(): Unit = runTest {
        whenever(repo.flowOfTargets()).thenReturn(
            flow<Result<NamesOfTargets>> {
                emit(InProgress())
                delay(2000)
                emit(Success(NamesOfTargets.emptyNamesOfTargets()))
            }
        )

        viewModel.collectScheduleTargets()

        assertThat(viewModel.listOfScheduleTargets.value).isInstanceOf(InProgress::class.java)

        advanceUntilIdle()

        viewModel.listOfScheduleTargets.value.let { result ->
            assertThat(result).isInstanceOf(Success::class.java)
            assertThat((result as Success).resultValue).isEqualTo(NamesOfTargets.emptyNamesOfTargets())
        }
    }

    @Test
    fun updateTargets_whenReturnsError_shouldUpdateTargetsAndSetToError(): Unit = runTest {

        whenever(repo.flowOfTargets()).thenReturn(
            flow<Result<NamesOfTargets>> {
                emit(InProgress())
                delay(2000)
                emit(Failure())
            }
        )

        viewModel.collectScheduleTargets()

        val inProgressValue = viewModel.listOfScheduleTargets.value
        assertThat(inProgressValue).isInstanceOf(InProgress::class.java)

        advanceUntilIdle()

        val failureValue = viewModel.listOfScheduleTargets.value
        assertThat(failureValue).isInstanceOf(Failure::class.java)
    }

    @Test
    fun flowOfLessonsSchedule_whenIsSuccessful_shouldUpdateScheduleForWeek() = runTest {

        val scheduleForWeek = listOf(ScheduleForDay.emptyScheduleForDay())
        whenever(repo.flowOfLessonsSchedule(any(), any())).thenReturn(
            flow {
                emit(InProgress())
                delay(2000)
                emit(Success(scheduleForWeek))
            }
        )

        viewModel.collectLessonsSchedule("SOME_NAME")
        assertThat(viewModel.scheduleForWeek.value).isInstanceOf(InProgress::class.java)

        advanceUntilIdle()

        viewModel.scheduleForWeek.value.let { result ->
            assertThat(result).isInstanceOf(Success::class.java)
            assertThat((result as Success).resultValue).isEqualTo(scheduleForWeek)
        }
    }

    @Test
    fun updateTargetsAndSchedule_whenIsSuccessful_shouldUpdateTargetsAndScheduleForWeek() =
        runTest {
            whenever(repo.flowOfTargets()).thenReturn(
                flow {
                    emit(InProgress())
                    delay(1000)
                    emit(Success(NamesOfTargets.emptyNamesOfTargets()))
                }
            )

            val scheduleForWeek = listOf(ScheduleForDay.emptyScheduleForDay())

            whenever(repo.flowOfLessonsSchedule(any(), any())).thenReturn(
                flow {
                    emit(InProgress())
                    delay(1000)
                    emit(Success(scheduleForWeek))
                }
            )

            viewModel.updateTargetsAndSchedule("SOME_STRING")

            assertThat(viewModel.listOfScheduleTargets.value).isInstanceOf(InProgress::class.java)
            assertThat(viewModel.scheduleForWeek.value).isInstanceOf(InProgress::class.java)

            advanceUntilIdle()

            val resultNames = viewModel.listOfScheduleTargets.value
            val resultTargets = viewModel.scheduleForWeek.value

            assertThat(resultNames).isInstanceOf(Success::class.java)
            assertThat((resultNames as Success).resultValue).isEqualTo(
                NamesOfTargets.emptyNamesOfTargets()
            )

            assertThat(resultTargets).isInstanceOf(Success::class.java)
            assertThat((resultTargets as Success).resultValue).isEqualTo(
                scheduleForWeek
            )
        }
}