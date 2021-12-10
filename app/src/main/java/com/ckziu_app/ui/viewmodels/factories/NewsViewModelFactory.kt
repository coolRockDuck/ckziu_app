package com.ckziu_app.ui.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ckziu_app.data.repositories.NewsRepository
import com.ckziu_app.ui.viewmodels.NewsViewModel
import kotlinx.coroutines.CoroutineDispatcher

class NewsViewModelFactory(
    private val repo: NewsRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(repo, ioDispatcher) as T
    }
}