package com.ckziu_app.data.repositories

import android.util.Log
import com.ckziu_app.data.network.LessonScheduleGetter
import com.ckziu_app.model.*
import kotlinx.coroutines.flow.flow

/** Repository managing informations regarding lessons schedule
 * @see com.ckziu_app.model.Lesson*/
class LessonsScheduleRepository(private val networkLessonScheduleGetter: LessonScheduleGetter) {

    companion object {
        const val TAG = "LessonsScheduleRepo"
    }

    /** Returns flow of lessons for whole week.*/
    suspend fun flowOfLessonsSchedule(
        targetNameGroup: String, listOfTargetsName: Result<NamesOfTargets>
    ) = flow<Result<List<ScheduleForDay>>> {
        emit(InProgress())
        Log.d(TAG, "updateSchedule: updating lesson schedule")

        val namesOfTargets = when (listOfTargetsName) {
            is Success -> {
                listOfTargetsName.resultValue
            }

            else -> null
        }

        networkLessonScheduleGetter.getTargetSchedule(
            targetNameGroup,
            namesOfTargets
        ).let { schedule ->
            when (schedule) {
                null -> emit(Failure())
                else -> emit(Success(schedule))
            }
        }
    }

    /** Returns flow of names of people or groups of people
     * which have their schedule. */
    suspend fun flowOfTargets() = flow<Result<NamesOfTargets>> {
        emit(InProgress())
        networkLessonScheduleGetter.getTargetsNames().let { targets ->
            when (targets) {
                null -> emit(Failure())
                else -> emit(Success(targets))
            }
        }
    }
}