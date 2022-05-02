package com.ckziu_app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.model.Failure
import com.ckziu_app.model.MainPageInfo
import com.ckziu_app.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainPageViewModel @Inject constructor(
    private val repository: MainPageRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    companion object {
        const val TAG = "MainPageViewModel"
    }

    /** Info scraped from main page of schools [website](http://ckziu.olawa.pl/).*/
    private val _mainPageInfo = MutableLiveData<Result<MainPageInfo>>()
    val mainPageInfo: LiveData<Result<MainPageInfo>> = _mainPageInfo

    init {
        collectMainPageInfo()
        Log.d(TAG, "creating main page view model")
    }

    fun collectMainPageInfo() {
        viewModelScope.launch {
            repository.flowOfMainPageInfo()
                .flowOn(ioDispatcher)
                .catch { e ->
                    Log.e(
                        LessonsScheduleViewModel.TAG,
                        "reloadNews: error occurred while executing this method, error = $e",
                    )
                    emit(Failure(error = e))
                }
                .collectLatest {
                        _mainPageInfo.value = it
                }
        }
    }
}