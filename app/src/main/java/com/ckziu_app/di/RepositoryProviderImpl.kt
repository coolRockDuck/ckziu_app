package com.ckziu_app.di

import android.content.Context
import android.util.Log
import com.ckziu_app.data.local.NewsDataBase
import com.ckziu_app.data.network.LessonScheduleGetter
import com.ckziu_app.data.network.MainPageInfoGetter
import com.ckziu_app.data.network.NewsGetter
import com.ckziu_app.data.repositories.LessonsScheduleRepository
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.data.repositories.NewsRepository

/** Implementation of [RepositoryProvider]. */
class RepositoryProviderImpl {

    companion object {
        const val TAG = "RepositoryProvider"
    }

    lateinit var mainPageRepository: MainPageRepository
        private set
    lateinit var newsRepository: NewsRepository
        private set
    lateinit var lessonsScheduleRepository: LessonsScheduleRepository
        private set

    fun createMainPageRepo(mainPageInfoGetter: MainPageInfoGetter) {
        if (::mainPageRepository.isInitialized) {
            Log.w(TAG, "createMainPageRepo: mainPageRepository has been already initialized")
        } else {
            mainPageRepository = MainPageRepository(mainPageInfoGetter)
        }
    }

    fun createNewsRepo(
        applicationContext: Context,
        newsGetter: NewsGetter,
        dataBase: NewsDataBase
    ) {
        if (::newsRepository.isInitialized) {
            Log.w(TAG, "createMainPageRepo: newsRepository has been already initialized")
        } else {
            newsRepository = NewsRepository(applicationContext, newsGetter, dataBase)
        }
    }

    fun createScheduleRepo(lessonScheduleGetter: LessonScheduleGetter) {
        if (::lessonsScheduleRepository.isInitialized) {
            Log.w(TAG, "createMainPageRepo: lessonsScheduleRepository has been already initialized")
        } else {
            lessonsScheduleRepository = LessonsScheduleRepository(lessonScheduleGetter)
        }
    }
}