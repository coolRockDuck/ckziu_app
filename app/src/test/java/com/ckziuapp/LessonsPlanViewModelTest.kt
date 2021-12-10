package com.ckziuapp

//import android.os.Build
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.google.common.truth.Truth.assertThat
//import getOrAwaitValue
//import getTestSuccessValue
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.test.TestCoroutineDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.annotation.Config
//import repository.Failure
//import repository.Success
//import viewmodels.LessonsPlanViewModel

//@ExperimentalCoroutinesApi
//@RunWith(AndroidJUnit4::class)
//@Config(sdk = [Build.VERSION_CODES.O_MR1])
//class LessonsPlanViewModelTest {
//
//    @get:Rule
//    val rule = InstantTaskExecutorRule()
//    private val testDispatcher = TestCoroutineDispatcher()
//    private lateinit var viewModel: LessonsPlanViewModel
//
//    @Before
//    fun setUp() {
//        viewModel = LessonsPlanViewModel(ApplicationProvider.getApplicationContext())
//        Dispatchers.setMain(testDispatcher)
//    }
//
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//        testDispatcher.cleanupTestCoroutines()
//    }
//
//    @Test
//    fun updateTargetsAndPlanTest_shouldUpdateTargetsAndPlan(): Unit = runBlocking(testDispatcher) {
//        viewModel.updatePlanTargets().join()
//        val targetNames = viewModel.listOfPlanTargets.getTestSuccessValue()
//
//        viewModel.updatePlanOfTarget(targetNames.groupNames.first()).join()
//        val targetsPlan = viewModel.planForWeek.getTestSuccessValue()
//        assertThat(targetsPlan).isNotEmpty()
//    }
//
//
//    @Test
//    fun updatePlanTargets_shouldUpdateTarget() = runBlocking(testDispatcher) {
//        viewModel.updatePlanTargets().join()
//
//        val listOfPlanTargets = viewModel.listOfPlanTargets.getTestSuccessValue()
//        val (groupNames, teachersNames, classroomNames) = listOfPlanTargets
//
//        assertThat(groupNames).isNotEmpty()
//        assertThat(teachersNames).isNotEmpty()
//        assertThat(classroomNames).isNotEmpty()
//
//        groupNames.forEach { nameOfGroup ->
//            assertThat(nameOfGroup).isNotEmpty()
//        }
//
//        teachersNames.forEach { nameOfTeacher ->
//            assertThat(nameOfTeacher).isNotEmpty()
//        }
//
//        classroomNames.forEach { nameOfClassroom ->
//            assertThat(nameOfClassroom).isNotEmpty()
//        }
//    }
//
//    @Test
//    fun updateTargetsAndPlan_shouldSetTargetsAsSuccessAndPlanAsFail_whenGivenEmptyInvalidNameOfTarget() {
//        runBlocking(testDispatcher) {
//            viewModel.updateTargetsAndPlan("WRONG_NAME").join()
//
//            val targetsNames = viewModel.listOfPlanTargets.getOrAwaitValue()
//            val targetPlan = viewModel.planForWeek.getOrAwaitValue()
//
//            assertThat(targetsNames is Success).isTrue()
//            assertThat(targetPlan is Failure).isTrue()
//        }
//    }
//
//
//    @Test
//    fun updatePlanOfTarget_shouldSetPlanAsFail_whenGivenInvalidNameOfTarget() {
//        runBlocking(testDispatcher) {
//            viewModel.updatePlanOfTarget("WRONG_NAME").join()
//
//            val targetPlan = viewModel.planForWeek.getOrAwaitValue()
//
//            assertThat(targetPlan is Failure).isTrue()
//        }
//    }
//}