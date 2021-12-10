package com.ckziu_app.data.repositories

import android.content.Context
import android.util.Log
import com.ckziu_app.data.*
import com.ckziu_app.data.local.NewsDataBase
import com.ckziu_app.data.network.NewsGetter
import com.ckziu_app.model.*
import com.ckziu_app.ui.activities.MainActivity
import com.ckziu_app.ui.fragments.NewsListFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.coroutineContext

/** Repository managing informations regarding news
 * @see com.ckziu_app.model.NewsPageInfo
 * @see com.ckziu_app.model.News
 * */

class NewsRepository(
    private val applicationContext: Context,
    private val newsGetter: NewsGetter,
    private val dataBase: NewsDataBase
) {

    companion object {
        const val TAG = "NewsRepository"
    }

    /** Loads news from local database if database contains news with
     *  [News.pageIndex] equal to [activeNewsPageIndex],if not
     *  then it fetches news from network via [NewsGetter].
     *
     * @see NewsRepository.reloadNews*/
    suspend fun reloadNews(activeNewsPageIndex: Int) = flow {
        emit(InProgress())
        val newsFormDB = loadNews(activeNewsPageIndex)

        when {
            !newsFormDB.isNullOrEmpty() -> {
                Log.i(
                    TAG,
                    "News are already in database, number of news = ${newsFormDB.size}"
                )
                emit(Success(NewsPageInfo(newsFormDB, null)))
            }
            else -> {
                Log.d(TAG, "reloadNews: Fetching news")
                emit(fetchAndSaveNews(activeNewsPageIndex))
            }
        }
    }

    /** Fetches news from the network and saves them if fetching succeeds. */
    private suspend fun fetchAndSaveNews(activeNewsPageIndex: Int): Result<NewsPageInfo> {
        Log.i(TAG, "Fetching news")

        val fetchedNews = fetchNewsAsync(activeNewsPageIndex).await()
        val localCoroutineContext = coroutineContext
        fetchedNews.ifSuccessThen { success ->
            // in this lambda function, active coroutine context is not accessible, but local variables are,
            // so declaring localCoroutineContext is needed
            CoroutineScope(localCoroutineContext).launch {
                saveNews(success.resultValue.listOfNewsResponses)
            }
        }

        fetchedNews.ifFailureThen { failure ->
            Log.w(TAG, "Error when fetched the news, ${failure.errorMsg}")
        }

        return fetchedNews
    }

    suspend fun loadArticleHmtl(targetNews: News) {
        targetNews.articleHtml = newsGetter.getArticleHtml(targetNews)
    }

    private suspend fun saveNews(listOfNews: List<News>) {
        //todo add paging
        withContext(Dispatchers.IO + Job()) {
            // saving all news it`s costly memory wise and also it`s hard to organize them properly,
            // so for now only last page of news is saved
            dataBase.newsDao().deleteAllNews()
            dataBase.newsDao().insertNews(*listOfNews.toTypedArray())
        }
    }

    private suspend fun fetchNewsAsync(activeNewsPageIndex: Int) =
        CoroutineScope(coroutineContext + Dispatchers.IO).async {
            val result = newsGetter.getNewsFormPage(activeNewsPageIndex)
            return@async if (result?.listOfNewsResponses.isNullOrEmpty()) {
                Failure("News from this page are null or empty, news = $result")
            } else {
                Success(result!!) // nullability is checked earlier
            }
        }

    private suspend fun loadNews(activeNewsPageIndex: Int) = withContext(Dispatchers.IO) {
        dataBase.newsDao().getAllNewsFromPage(activeNewsPageIndex)
    }

    /** Loads number of pages of news. */
    fun loadMaxPageIndex(): Int {
        applicationContext.getSharedPreferences(
            MainActivity.PREFERENCES_KEY,
            Context.MODE_PRIVATE
        ).run {
            return getInt(NewsListFragment.MAX_PAGE_INDEX, 1)
        }
    }

    /** Saves number of pages of news. */
    fun saveMaxPageIndex(maxPageIndex: Result<Int>) {
        val newMaxPageIndex = maxPageIndex.let {
            if (it is Success) {
                it.resultValue
            } else {
                return
            }
        }

        applicationContext.getSharedPreferences(
            MainActivity.PREFERENCES_KEY,
            Context.MODE_PRIVATE
        ).edit().run {
            putInt(NewsListFragment.MAX_PAGE_INDEX, newMaxPageIndex)
            apply()
        }
    }

    /** Saves  index of page which was selected last time
     *  the app was closed */
    fun saveActivePageIndex(activePageIndex: Int) {
        applicationContext.getSharedPreferences(
            MainActivity.PREFERENCES_KEY,
            Context.MODE_PRIVATE
        ).edit().run {
            putInt(
                NewsListFragment.LAST_ACTIVE_PAGE_INDEX,
                activePageIndex
            )

            apply()
        }
    }

    /** Loads index of page which was selected before
     *  the app was closed last time. */
    fun loadLastActivePageIndex(): Int {
        return applicationContext.getSharedPreferences(
            MainActivity.PREFERENCES_KEY,
            Context.MODE_PRIVATE
        ).run {
            getInt(
                NewsListFragment.LAST_ACTIVE_PAGE_INDEX,
                1
            )
        }
    }

}