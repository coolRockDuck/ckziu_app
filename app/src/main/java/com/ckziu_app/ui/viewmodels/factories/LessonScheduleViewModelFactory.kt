package com.ckziu_app.ui.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ckziu_app.data.repositories.LessonsScheduleRepository
import com.ckziu_app.ui.viewmodels.LessonsScheduleViewModel
import kotlinx.coroutines.CoroutineDispatcher

class LessonScheduleViewModelFactory(private val repo: LessonsScheduleRepository, private val ioDispatcher: CoroutineDispatcher): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LessonsScheduleViewModel(repo, ioDispatcher) as T
    }
}