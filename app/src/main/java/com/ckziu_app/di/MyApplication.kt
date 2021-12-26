package com.ckziu_app.di

import android.app.Application
import com.ckziu_app.data.local.NewsDataBase
import com.ckziu_app.data.network.LessonScheduleGetter
import com.ckziu_app.data.network.MainPageInfoGetter
import com.ckziu_app.data.network.NewsGetter
import com.ckziu_app.data.repositories.LessonsScheduleRepository
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.data.repositories.NewsRepository

class MyApplication : Application(), RepositoryProvider {

    /** This object is needed in order to manually injects
     * repositories.
     * Works just like a AppContainer
     * [link](https://developer.android.com/training/dependency-injection/manual#dependencies-container)*/
    private val repositoryProviderImpl = RepositoryProviderImpl()

    override fun onCreate() {
        super.onCreate()
        initDI()
    }

    override fun getMainPageRepo(): MainPageRepository {
        return repositoryProviderImpl.mainPageRepository
    }

    override fun getNewsRepo(): NewsRepository {
        return repositoryProviderImpl.newsRepository
    }

    override fun getScheduleRepo(): LessonsScheduleRepository {
        return repositoryProviderImpl.lessonsScheduleRepository
    }

    /** Creates all dependencies.
     * @see RepositoryProvider*/
    private fun initDI() {
        repositoryProviderImpl.run {
            createMainPageRepo(MainPageInfoGetter())
            createNewsRepo(
                applicationContext,
                NewsGetter(),
                NewsDataBase.getInstance(applicationContext)
            )
            createScheduleRepo(LessonScheduleGetter())
        }
    }
}