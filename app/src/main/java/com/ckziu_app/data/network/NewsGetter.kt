package com.ckziu_app.data.network

import android.util.Log
import com.ckziu_app.model.News
import com.ckziu_app.model.NewsPageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

/** Singleton that is scraping info about news from schools
 *  [website](http://ckziu.olawa.pl/aktualnosci/).*/
class NewsGetter @Inject constructor() {

    companion object {
        private const val TIMEOUT_MILS = 7_500
        private const val TAG = "NewsGetter"
    }

    /** Loads List of [News] from schools [website][http://ckziu.olawa.pl/]*/
    suspend fun getNewsFormPage(pageOfNews: Int = 1): NewsPageInfo? {
        return withContext(Dispatchers.IO) {
            fetchNews(pageOfNews) // returning
        }
    }

    private fun fetchNews(pageOfNews: Int): NewsPageInfo? {
        val doc = getPageDoc(pageOfNews) ?: return null
        val maxPageIndex = getMaxIndexOfNewsPage(doc) ?: return null

        val newsList = mutableListOf<News>()

        doc.select("article").forEach { articleElement ->
            val title = getTitle(articleElement)
            val textPreview = getTextPreview(articleElement)
            val linkToFullText = getLinkToArticle(articleElement)
            val dateOfCreation = getCreationDate(articleElement)
            val linkToImage = getImageLink(articleElement)
            val creatorName = getCreatorName(articleElement)

            newsList.add(
                News(
                    title,
                    textPreview,
                    pageOfNews,
                    linkToFullText,
                    null,   // this will be fetched later fg. - when user clicks on the news card
                    linkToImage,
                    dateOfCreation,
                    creatorName
                )
            )
        }

        return NewsPageInfo(newsList, maxPageIndex)
    }

    /** Returns a [Document] from a of page containing news of [pageOfNews] index.
     * If [pageOfNews] is over maximum index of pages then returns null.*/
    private fun getPageDoc(pageOfNews: Int): Document? {

        if (pageOfNews <= 0) {
            Log.e(TAG, "getPageDoc: Page of news smaller or equal to 0, pageOfNews = $pageOfNews")
            return null
        }

        // when given 1 as page of index
        // it is redirecting to http://ckziu.olawa.pl/aktualnosci - (first page with news)
        // or http://ckziu.olawa.pl/aktualnosci/page/1
        val newsLink = when (pageOfNews) {
            1 -> "http://ckziu.olawa.pl/aktualnosci/"
            else -> "http://ckziu.olawa.pl/aktualnosci/page/$pageOfNews"
        }

        return try {
            // even if index of page is larger than max index of news page,
            // connection will be successful, but page will not contain any articles
            val doc = Jsoup.connect(newsLink).timeout(TIMEOUT_MILS).get()
            if (doc.getElementsByTag("article").isEmpty()) {
                null
            } else {
                doc
            }

        } catch (e: Exception) {
            Log.e("JSOUP", "Error: $e")
            null
        }
    }

    private fun getTitle(articleElement: Element): String {
        return getTitleElement(articleElement).text()
    }

    private fun getLinkToArticle(articleElement: Element) =
        getTitleElement(articleElement).attr("href")

    private fun getImageLink(articleElement: Element): String? {
        return try {
            articleElement.getElementsByTag("img")[0].attr("src")
        } catch (e: Exception) {
            return null
        }
    }

    /** Returns only title element without wrapper elements*/
    private fun getTitleElement(articleElement: Element): Element {
        val titleWithWrapper =
            articleElement.getElementsByClass("cmsmasters_post_title entry-title")[0]
        return titleWithWrapper.getElementsByTag("a")[0]
    }

    private fun getTextPreview(articleElement: Element): String? {
        val textPreviewWithWrapper =
            articleElement.getElementsByClass("cmsmasters_post_content entry-content")
        return if (textPreviewWithWrapper.isNotEmpty()) {
            val textPreview = textPreviewWithWrapper[0].getElementsByTag("p")[0].text()
            textPreview
        } else {
            null
        }
    }

    private fun getCreationDate(articleElement: Element) =
        articleElement.getElementsByClass("dn date updated")[0].text()

    private fun getCreatorName(articleElement: Element): String =
        articleElement.getElementsByClass("fn").text()

    /** Returns index of last page containing news.*/
    private fun getMaxIndexOfNewsPage(element: Element): Int? {
        return try {
            val pageIndexElements = element.getElementsByClass("page-numbers")
            val lastEle = pageIndexElements.last()
            val nextToLast = pageIndexElements[pageIndexElements.lastIndex - 1]

            when {
                (lastEle.getElementsByTag("a").isNotEmpty()) -> nextToLast.text().toInt()
                else -> lastEle.text().toInt()
            }

        } catch (e: Exception) {
            null
        }
    }

    suspend fun getArticleHtml(news: News): String? {
        return try {
            withContext(Dispatchers.IO) {
                news.linkToFullArticle?.let { link ->
                    val doc = Jsoup.connect(link).get()
                    val res = doc.getElementsByTag("article")[0]
                    // removing unnecessary elements
                    res.getElementsByClass("cmsmasters_post_category").remove()
                    res.html()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred when loading article html, error = $e")
            null
        }
    }
}



