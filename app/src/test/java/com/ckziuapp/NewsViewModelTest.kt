package com.ckziuapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ckziu_app.data.repositories.NewsRepository
import com.ckziu_app.model.*
import com.ckziu_app.ui.viewmodels.NewsViewModel
import com.ckziu_app.ui.viewmodels.factories.NewsViewModelFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

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


    @Captor
    private lateinit var argCaptorActivePageIndex: ArgumentCaptor<Int>

    @Captor
    private lateinit var argCaptorMaxPageIndex: ArgumentCaptor<Result<Int>>

    @Captor
    private lateinit var argCaptorActiveNews: ArgumentCaptor<Result<List<News>>>

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
    fun givenNewsPageInfo_whenReloadNews_shouldReturnTheSameValues() = runBlockingTest {
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

        verify(observerMaxPage, times(2)).onChanged(capture(argCaptorMaxPageIndex))
        verify(observerActiveNews, times(2)).onChanged(capture(argCaptorActiveNews))

        assertThat(argCaptorMaxPageIndex.value).isInstanceOf(InProgress::class.java)
        assertThat(argCaptorActiveNews.value).isInstanceOf(InProgress::class.java)

        testDispatcher.advanceTimeBy(2000)

        verify(observerMaxPage, times(3)).onChanged(capture(argCaptorMaxPageIndex))
        verify(observerActiveNews, times(3)).onChanged(capture(argCaptorActiveNews))

        assertThat(argCaptorMaxPageIndex.value).isInstanceOf(Success::class.java)
        assertThat((argCaptorMaxPageIndex.value as Success).resultValue).isEqualTo(maxPageIndex)

        assertThat(argCaptorActiveNews.value).isInstanceOf(Success::class.java)
        assertThat((argCaptorActiveNews.value as Success).resultValue).isEqualTo(emptyList)
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
    fun changePageAndFetchNews_whenRepoReturnsNewsPageInfo_shouldUpdateNewsAndChangePage() = runBlockingTest {
        val emptyListOfNews = emptyList<News>()
        val newPageIndex = 4
        val maxPageIndex = 20

        whenever(repo.loadMaxPageIndex()).thenReturn(20)

        whenever(repo.reloadNews(anyInt())).thenReturn(flow {
            emit(InProgress())
            delay(2000)
            emit(Success(NewsPageInfo(emptyListOfNews, maxPageIndex)))
        })

        newsViewModel.changePageAndFetchNews(newPageIndex)

        verify(observerMaxPage, times(2)).onChanged(capture(argCaptorMaxPageIndex))
        verify(observerActiveNews, times(2)).onChanged(capture(argCaptorActiveNews))
        verify(observerActivePageIndex, times(2)).onChanged(capture(argCaptorActivePageIndex))


        assertThat(argCaptorMaxPageIndex.value).isInstanceOf(InProgress::class.java)
        assertThat(argCaptorActiveNews.value).isInstanceOf(InProgress::class.java)

        testDispatcher.advanceTimeBy(2000)

        verify(observerMaxPage, times(3)).onChanged(capture(argCaptorMaxPageIndex))
        verify(observerActiveNews, times(3)).onChanged(capture(argCaptorActiveNews))

        assertThat(argCaptorActivePageIndex.value).isEqualTo(newPageIndex)
        assertThat(argCaptorMaxPageIndex.value).isInstanceOf(Success::class.java)
        assertThat(argCaptorActiveNews.value).isInstanceOf(Success::class.java)
        assertThat((argCaptorMaxPageIndex.value as Success).resultValue).isEqualTo(maxPageIndex)
        assertThat((argCaptorActiveNews.value as Success).resultValue).isEqualTo(emptyListOfNews)
    }
}