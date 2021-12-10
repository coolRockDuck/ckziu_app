package com.ckziu_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ckziu_app.model.News

/** Database containing news. */
@Database(entities = [News::class], version = 1, exportSchema = false)
abstract class NewsDataBase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {

        private const val DATABASE_NAME = "newsDataBase.db"

        @Volatile
        private lateinit var instance: NewsDataBase

        fun getInstance(ctx: Context): NewsDataBase {
            return synchronized(this) {
                if (Companion::instance.isInitialized) {
                    instance
                } else {
                    createDataBase(ctx).also { instance = it }
                }
            }
        }

        private fun createDataBase(ctx: Context): NewsDataBase {
            return Room.databaseBuilder(ctx, NewsDataBase::class.java, DATABASE_NAME).build()
        }
    }
}