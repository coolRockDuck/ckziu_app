package com.ckziu_app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ckziu_app.data.repositories.NewsRepository
import com.ckziu_app.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class NewsViewModel constructor(
    private val repository: NewsRepository,
    private val IoDispatcher: CoroutineDispatcher,
) : ViewModel() {

    // todo add refresh of maxPageIndex
    companion object {
        const val TAG = "NewsViewModel"
    }

    /** Info about news and list of news from schools [website](http://ckziu.olawa.pl/aktualnosci/)*/
    private val _activePageNews = MutableLiveData<Result<List<News>>>()
    val activePageNews: LiveData<Result<List<News>>> = _activePageNews

    /** Number of news pages. */
    private val _maxPageIndex = MutableLiveData<Result<Int>>(Success(1))
    val maxPageIndex: LiveData<Result<Int>> = _maxPageIndex

    /** Index of a active page of news.
     *  Should be changed by [setActivePageIndex]*/
    private val _activeNewsPageIndex = MutableLiveData(1)
    val activeNewsPageIndex: LiveData<Int> = _activeNewsPageIndex

    init {
        changeActivePageIndex(loadLastActivePageIndex())
        refreshNewsList()
    }


    private fun setActivePageIndex(newIndex: Int) {
        if (maxPageIndex.value!! !is Success) {
            Log.w("setActivePageIndex", "maxPageIndex is not successful ")
            _activeNewsPageIndex.value = newIndex
            return
        }

        when {
            newIndex <= 0 -> {
                val error = IllegalArgumentException(
                    "Index can't be smaller than or equal to 0 : index = $newIndex"
                )

                Log.e(TAG, "setActivePage: ", error)
            }

            (maxPageIndex.value!! as Success).resultValue < newIndex -> {
                val maxPageIndexValue = (maxPageIndex.value as Success).resultValue
                val error = IllegalArgumentException(
                    "Index can't be greater than max index of news page:" +
                            " index = $newIndex," +
                            " maxIndex = $maxPageIndexValue"
                )

                Log.e(TAG, "setActivePage: ", error)
            }

            else -> {
                _activeNewsPageIndex.value = newIndex
                return
            }
        }

        Log.e(
            "NewsViewModel",
            "setActivePageIndex: error while setting activePage: $newIndex, maxPageIndex: ${(_maxPageIndex.value as Success).resultValue}"
        )
    }

    private fun changePageAndReloadNews(newPageIndex: Int) {
        setActivePageIndex(newPageIndex)
        refreshNewsList()
    }

    private fun changeActivePageIndex(newIndex: Int) {
        setActivePageIndex(newIndex)
    }

    /** Reloads news from database or fetches them from network.
     * @see NewsRepository.reloadNews*/
    fun refreshNewsList() = viewModelScope.launch {
        repository.reloadNews(activeNewsPageIndex.value!!)
            .flowOn(IoDispatcher)
            .catch { e ->
                Log.e(
                    TAG,
                    "reloadNews: error occurred while executing this method, error = $e",
                )
                emit(Failure(error = e))
            }
            .collectLatest { result ->
                when (result) {
                    is Success -> {
                        result.resultValue.maxPageIndex.let { maxIndex ->
                            when (maxIndex) {
                                null -> _maxPageIndex.value = Success(repository.loadMaxPageIndex())
                                else -> _maxPageIndex.value = Success(maxIndex)
                            }
                        }
                    }

                    is InProgress -> {
                        _maxPageIndex.value = InProgress()
                    }
                    is Failure -> {
                        _maxPageIndex.value = Failure(result.errorMsg, result.error)
                    }
                }

                _activePageNews.value = NewsPageInfo.fromNewsPageInfoResultToNewsResult(result)
            }
    }


    /** Calls [setActivePageIndex] and then fetches news from new page. */
    fun changePageAndFetchNews(index: Int) = viewModelScope.launch {
        changePageAndReloadNews(index)
    }

    /** Loads [News.articleHtml] about news and saves inside instance of the News.
     * @see News.articleHtml */
    fun loadArticleHtml(targetNews: News) = viewModelScope.launch {
        repository.loadArticleHmtl(targetNews)
    }

    fun saveMaxPageIndex() {
        repository.saveMaxPageIndex(maxPageIndex.value!!)
    }

    /**  Saves  index of page which was selected last time the app was closed */
    fun saveActivePageIndex() {
        activeNewsPageIndex.value?.let { index ->
            repository.saveActivePageIndex(index)
        }
    }

    /** Loads index of page which was selected last time the app was closed*/
    private fun loadLastActivePageIndex(): Int = repository.loadLastActivePageIndex()
}