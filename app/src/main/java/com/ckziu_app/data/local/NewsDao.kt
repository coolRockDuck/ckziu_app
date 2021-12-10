package com.ckziu_app.data.local

import androidx.room.*
import com.ckziu_app.model.News

@Dao
interface NewsDao {

    @Query("SELECT * FROM news")
    fun getAllNews(): List<News>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNews(vararg news: News)

    @Update
    fun updateNews(vararg updatedNews: News)

    @Query("SELECT * FROM news WHERE newsID IS (:id) LIMIT 1")
    fun getNewsById(id: Int): News

    @Query("SELECT * FROM news WHERE title IS (:name) LIMIT 1")
    fun getNewsByTitle(name: String): News

    @Query("SELECT * FROM news WHERE pageIndex IS :indexOfPage")
    fun getAllNewsFromPage(indexOfPage: Int): List<News>

    @Query("DELETE FROM news")
    fun deleteAllNews(): Int
}