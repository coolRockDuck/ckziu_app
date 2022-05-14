package com.ckziuapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ckziu_app.data.repositories.LessonsScheduleRepository
import com.ckziu_app.model.*
import com.ckziu_app.ui.viewmodels.LessonsScheduleViewModel
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
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
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LessonsScheduleViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var viewModel: LessonsScheduleViewModel

    @Mock
    private lateinit var repo: LessonsScheduleRepository

    @Mock
    private lateinit var observerNamesOfTargets: Observer<Result<NamesOfTargets>>

    @Mock
    private lateinit var observerSchedulesForDays: Observer<Result<List<ScheduleForDay>>>

    @Captor
    private lateinit var argCaptorNamesOfTargets: ArgumentCaptor<Result<NamesOfTargets>>

    @Captor
    private lateinit var argCaptorSchedulesForDays: ArgumentCaptor<Result<List<ScheduleForDay>>>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = LessonsScheduleViewModel(
            repo,
            testDispatcher
        ).apply {
            listOfScheduleTargets.observeForever(observerNamesOfTargets)
            scheduleForWeek.observeForever(observerSchedulesForDays)
        }
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun updateTargets_shouldUpdateTargets(): Unit = runBlockingTest {
        whenever(repo.flowOfTargets()).thenReturn(
            flow<Result<NamesOfTargets>> {
                emit(InProgress())
                delay(2000)
                emit(Success(NamesOfTargets.emptyNamesOfTargets()))
            }
        )

        viewModel.collectScheduleTargets()
        verify(observerNamesOfTargets, times(2)).onChanged(capture(argCaptorNamesOfTargets))
        assertThat(argCaptorNamesOfTargets.value).isInstanceOf(InProgress::class.java)

        testDispatcher.advanceTimeBy(2000)

        verify(observerNamesOfTargets, times(3)).onChanged(capture(argCaptorNamesOfTargets))

        assertThat(argCaptorNamesOfTargets.value).isInstanceOf(Success::class.java)
        assertThat((argCaptorNamesOfTargets.value as Success).resultValue).isEqualTo(NamesOfTargets.emptyNamesOfTargets())
    }

    @Test
    fun updateTargets_whenReturnsError_shouldUpdateTargetsAndSetToError(): Unit = runBlockingTest {

        whenever(repo.flowOfTargets()).thenReturn(
            flow<Result<NamesOfTargets>> {
                emit(InProgress())
                delay(2000)
                emit(Failure())
            }
        )

        viewModel.collectScheduleTargets()
        verify(observerNamesOfTargets, times(2)).onChanged(capture(argCaptorNamesOfTargets))
        assertThat(argCaptorNamesOfTargets.value).isInstanceOf(InProgress::class.java)

        testDispatcher.advanceTimeBy(2000)

        verify(observerNamesOfTargets, times(3)).onChanged(capture(argCaptorNamesOfTargets))

        assertThat(argCaptorNamesOfTargets.value).isInstanceOf(Failure::class.java)
    }

    @Test
    fun flowOfLessonsSchedule_whenIsSuccessful_shouldUpdateScheduleForWeek() = runBlockingTest {

        val scheduleForWeek = listOf(ScheduleForDay.emptyScheduleForDay())
        whenever(repo.flowOfLessonsSchedule(any(), any())).thenReturn(
            flow {
                emit(InProgress())
                delay(2000)
                emit(Success(scheduleForWeek))
            }
        )

        viewModel.collectLessonsSchedule("SOME_NAME")
        verify(observerSchedulesForDays, times(1)).onChanged(capture(argCaptorSchedulesForDays))
        assertThat(argCaptorSchedulesForDays.value).isInstanceOf(InProgress::class.java)

        testDispatcher.advanceTimeBy(2000)

        verify(observerSchedulesForDays, times(2)).onChanged(capture(argCaptorSchedulesForDays))

        assertThat(argCaptorSchedulesForDays.value).isInstanceOf(Success::class.java)
        assertThat((argCaptorSchedulesForDays.value as Success).resultValue).isEqualTo(
            scheduleForWeek
        )
    }

    @Test
    fun updateTargetsAndSchedule_whenIsSuccessful_shouldUpdateTargetsAndScheduleForWeek() =
        runBlockingTest {
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
            verify(observerNamesOfTargets, times(2)).onChanged(capture(argCaptorNamesOfTargets))
            verify(observerSchedulesForDays, times(1)).onChanged(capture(argCaptorSchedulesForDays))

            assertThat(argCaptorNamesOfTargets.value).isInstanceOf(InProgress::class.java)
            assertThat(argCaptorSchedulesForDays.value).isInstanceOf(InProgress::class.java)

            testDispatcher.advanceTimeBy(2000)

            verify(observerNamesOfTargets, times(3)).onChanged(capture(argCaptorNamesOfTargets))
            verify(observerSchedulesForDays, times(2)).onChanged(capture(argCaptorSchedulesForDays))

            assertThat(argCaptorNamesOfTargets.value).isInstanceOf(Success::class.java)
            assertThat(argCaptorSchedulesForDays.value).isInstanceOf(Success::class.java)

            assertThat((argCaptorNamesOfTargets.value as Success).resultValue).isEqualTo(
                NamesOfTargets.emptyNamesOfTargets()
            )
            assertThat((argCaptorSchedulesForDays.value as Success).resultValue).isEqualTo(
                scheduleForWeek
            )
        }
}