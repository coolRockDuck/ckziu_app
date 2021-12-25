package com.ckziu_app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ckziu_app.data.repositories.LessonsScheduleRepository
import com.ckziu_app.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class LessonsScheduleViewModel(
    private val repository: LessonsScheduleRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    companion object {
        const val TAG = "LessonsScheduleVM"
    }

    /** List of [days][ScheduleForDay] which every one of which contains [Lesson].*/
    private val _scheduleForWeek = MutableLiveData<Result<List<ScheduleForDay>>>()
    val scheduleForWeek: LiveData<Result<List<ScheduleForDay>>> = _scheduleForWeek

    private val _listOfScheduleTargets = MutableLiveData<Result<NamesOfTargets>>()
    val listOfScheduleTargets: LiveData<Result<NamesOfTargets>> = _listOfScheduleTargets


    init {
        collectScheduleTargets().invokeOnCompletion {
            listOfScheduleTargets.value?.ifSuccessThen { success ->
                collectLessonsSchedule(success.resultValue.groupNames[0])
            }
        }
    }

    fun updateTargetsAndSchedule(targetName: String) = viewModelScope.launch {
        collectScheduleTargets()
        collectLessonsSchedule(targetName)
    }

    fun collectScheduleTargets() = viewModelScope.launch {
        repository.flowOfTargets()
            .flowOn(ioDispatcher)
            .catch { e ->
                Log.e(
                    TAG,
                    "reloadNews: error occurred while executing this method, error = $e",
                )
                emit(Failure(error = e))
            }
            .collectLatest { result ->
                _listOfScheduleTargets.value = result
            }
    }

    fun collectLessonsSchedule(targetName: String) = viewModelScope.launch {
        repository.flowOfLessonsSchedule(targetName, listOfScheduleTargets.value!!)
            .flowOn(ioDispatcher)
            .catch { e ->
                Log.e(
                    TAG,
                    "reloadNews: error occurred while executing this method, error = $e",
                )
                emit(Failure(error = e))
            }
            .collectLatest { result ->
                _scheduleForWeek.value = result
            }
    }
}