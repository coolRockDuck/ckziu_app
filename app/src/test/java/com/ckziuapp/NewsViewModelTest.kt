package com.ckziuapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ckziu_app.model.InProgress
import com.ckziu_app.model.Result
import com.ckziu_app.model.Success
import com.ckziu_app.data.repositories.NewsRepository
import com.ckziu_app.model.News
import com.ckziu_app.model.NewsPageInfo
import com.ckziu_app.ui.viewmodels.NewsViewModel
import com.ckziu_app.ui.viewmodels.factories.NewsViewModelFactory
import com.nhaarman.mockitokotlin2.whenever
import getTestSuccessValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import com.nhaarman.mockitokotlin2.any

@RunWith(JUnit4::class)
@ExperimentalCoroutinesApi
class NewsViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var newsViewModel: NewsViewModel

    @Mock
    private lateinit var repo: NewsRepository

    @Mock
    lateinit var observerActivePageIndex: Observer<Int>

    @Mock
    lateinit var observerMaxPage: Observer<Result<Int>>

    @Mock
    lateinit var observerActiveNews: Observer<Result<List<News>>>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        newsViewModel =
            NewsViewModelFactory(repo, testDispatcher).create(NewsViewModel::class.java).apply {
                activePageNews.observeForever(observerActiveNews)
                maxPageIndex.observeForever(observerMaxPage)
                activeNewsPageIndex.observeForever(observerActivePageIndex)
            }

    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun givenNewsPageInfo_whenReloadNews_shouldReturnTheSameValues() = runBlockingTest() {
        val inProgress = InProgress<NewsPageInfo>()
        val emptyList = emptyList<News>()
        val success = Success(NewsPageInfo(emptyList, 20))
        whenever(repo.reloadNews(anyInt())).thenReturn(flow {
            emit(inProgress)
            emit(success)
        })

        newsViewModel.refreshNewsList()

        assertTrue(newsViewModel.activePageNews.value is Success)
        assertEquals(emptyList, newsViewModel.activePageNews.getTestSuccessValue())
        assertEquals(20, newsViewModel.maxPageIndex.getTestSuccessValue())
//        assertEquals(20, (newsViewModel.maxPageIndex.value as Success).resultValue)
    }


    @Test
    fun givenHtml_whenLoadArticleHtml_shouldReturnHtml() = runBlocking {
        whenever(repo.loadArticleHmtl(any())).then {
            (it.arguments.first() as News).articleHtml = "MY HTML"
            return@then Unit
        }

        val emptyNews = News.createEmptyNews()
        newsViewModel.loadArticleHtml(emptyNews)

        assertEquals("MY HTML", emptyNews.articleHtml)
    }

    @Test
    fun some() = runBlockingTest {
        val emptyListOfNews = emptyList<News>()
        val newPageIndex = 4
        whenever(repo.reloadNews(anyInt())).thenReturn(flow {
            emit(InProgress())
            emit(Success(NewsPageInfo(emptyListOfNews, 20)))
        })

        whenever(repo.loadMaxPageIndex()).thenReturn(20)

        newsViewModel.changePageAndFetchNews(newPageIndex)

        assertEquals(emptyListOfNews, newsViewModel.activePageNews.getTestSuccessValue())
        assertEquals(20, newsViewModel.maxPageIndex.getTestSuccessValue())
    }

}