package com.ckziu_app.data.local

import android.content.Context
import android.util.Log
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
        const val TAG = "NewsDataBase"

        fun createInstance(context: Context): NewsDataBase {
            Log.d(TAG, "createInstance: ")
            return Room.databaseBuilder(context, NewsDataBase::class.java, DATABASE_NAME).build()
        }
    }
}