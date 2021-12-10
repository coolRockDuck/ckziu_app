package com.ckziu_app.di

import com.ckziu_app.data.repositories.LessonsScheduleRepository
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.data.repositories.NewsRepository

/** Interface used for manual injection of repositories. */
interface RepositoryProvider {
    fun getMainPageRepo(): MainPageRepository
    fun getNewsRepo(): NewsRepository
    fun getScheduleRepo(): LessonsScheduleRepository
}

