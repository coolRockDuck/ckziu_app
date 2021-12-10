package com.ckziu_app.ui.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.ui.viewmodels.MainPageViewModel
import kotlinx.coroutines.CoroutineDispatcher

class MainPageViewModelFactory(private val repo: MainPageRepository, private val ioDispatcher: CoroutineDispatcher): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainPageViewModel(repo, ioDispatcher) as T
    }
}