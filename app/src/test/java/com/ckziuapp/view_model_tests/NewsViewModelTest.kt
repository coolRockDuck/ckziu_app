package com.ckziuapp.view_model_tests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ckziu_app.data.repositories.NewsRepository
import com.ckziu_app.model.InProgress
import com.ckziu_app.model.News
import com.ckziu_app.model.NewsPageInfo
import com.ckziu_app.model.Success
import com.ckziu_app.ui.viewmodels.NewsViewModel
import com.ckziuapp.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyInt
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class NewsViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var newsViewModel: NewsViewModel

    @Mock
    private lateinit var repo: NewsRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        newsViewModel = NewsViewModel(repo, mainDispatcherRule.testDispatcher)
    }

    @Test
    fun givenNewsPageInfo_whenReloadNews_shouldReturnTheSameValues() = runTest {
        val emptyList = emptyList<News>()
        val maxPageIndex = 20
        whenever(repo.reloadNews(anyInt())).thenReturn(
            flow {
                emit(InProgress())
                delay(2000)
                emit(Success(NewsPageInfo(emptyList, maxPageIndex)))
            }
        )

        newsViewModel.refreshNewsList()

        var resultMaxPageInfo = newsViewModel.maxPageIndex.value

        assertThat(resultMaxPageInfo).isInstanceOf(InProgress::class.java)

        advanceUntilIdle()

        resultMaxPageInfo = newsViewModel.maxPageIndex.value
        val resultActiveNewsIndex = newsViewModel.activeNewsPageIndex.value

        assertThat(resultMaxPageInfo).isInstanceOf(Success::class.java)
        assertThat((resultMaxPageInfo as Success).resultValue).isEqualTo(maxPageIndex)

        assertThat(resultActiveNewsIndex).isEqualTo(1)
    }


    @Test
    fun givenHtml_whenLoadArticleHtml_shouldReturnHtml() = runTest {
        whenever(repo.loadArticleHmtl(any())).then {
            (it.arguments.first() as News).articleHtml = "MY HTML"
            return@then Unit
        }

        val emptyNews = News.createEmptyNews()

        newsViewModel.loadArticleHtml(emptyNews)

        assertEquals("MY HTML", emptyNews.articleHtml)
    }

    @Test
    fun changePageAndFetchNews_whenRepoReturnsNewsPageInfo_shouldUpdateNewsAndChangePage() =
        runTest {
            val emptyListOfNews = emptyList<News>()
            val newPageIndex = 4
            val maxPageIndex = 20

            whenever(repo.reloadNews(anyInt())).thenReturn(flow {
                emit(InProgress())
                delay(2000)
                emit(Success(NewsPageInfo(emptyListOfNews, maxPageIndex)))
            })

            newsViewModel.changePageAndFetchNews(newPageIndex)

            var resultMaxPage = newsViewModel.maxPageIndex.value
            var resultActiveIndex = newsViewModel.activePageNews.value

            assertThat(resultMaxPage).isInstanceOf(InProgress::class.java)
            assertThat(resultActiveIndex).isInstanceOf(InProgress::class.java)

            advanceUntilIdle()

            resultMaxPage = newsViewModel.maxPageIndex.value
            resultActiveIndex = newsViewModel.activePageNews.value

            assertThat(resultMaxPage).isInstanceOf(Success::class.java)
            assertThat(resultActiveIndex).isInstanceOf(Success::class.java)
            assertThat((resultActiveIndex as Success).resultValue).isEqualTo(emptyListOfNews)
            assertThat((resultMaxPage as Success).resultValue).isEqualTo(maxPageIndex)
        }
}