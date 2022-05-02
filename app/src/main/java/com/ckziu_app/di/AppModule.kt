package com.ckziu_app.di

import android.content.Context
import com.ckziu_app.data.local.NewsDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideIODispatcher() = Dispatchers.IO

    @Provides
    @Singleton
    fun provideNewsDataBase(@ApplicationContext applicationContext: Context): NewsDataBase  {
        return NewsDataBase.createInstance(applicationContext)
    }
}