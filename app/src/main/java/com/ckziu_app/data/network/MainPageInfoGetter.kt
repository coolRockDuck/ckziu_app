package com.ckziu_app.data.network

import android.util.Log
import com.ckziu_app.model.MainPageInfo
import com.ckziu_app.model.News
import com.ckziu_app.model.PromoNumbers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/** Class which fetches information about the main page.
 *
 * @see MainPageInfo*/
class MainPageInfoGetter {

    companion object {
        private const val TAG = "MainPageInfoGetter"

        /** Link to the main page*/
        private const val MAINPAGE_URL = "http://ckziu.olawa.pl/"

        /** Timeout for a request from  a web page */
        private const val MAINPAGE_TIMEOUT = 7_500
    }

    /** Fetches info the main page and returns [MainPageInfo]*/
    suspend fun getMainPageInfo(): MainPageInfo? {
        return withContext(Dispatchers.IO) {
             try {
                val doc = Jsoup.connect(MAINPAGE_URL).timeout(MAINPAGE_TIMEOUT).get()
                parseMainPageInfo(doc)
            } catch (e: Exception) {
                Log.e(TAG, "App can NOT get mainPageInfo, caused by: $e")
                null
            }
        }
    }


    private fun parseMainPageInfo(doc: Document): MainPageInfo {
        val columnsOfElements =
            getElementsInColumns(doc) // all elements we want are splitted in 4 columns

        val promoPhotosLinks = getPromoPhotoLinks(columnsOfElements[0])
        val title = getTitle(columnsOfElements[1])
        val promoText = getPromoText(columnsOfElements[1])
        val miniatureNews = getMiniatureNews(columnsOfElements[2])
        val promoNumbers = getPromoNumbers(columnsOfElements[3])

        return MainPageInfo(title, promoText, promoPhotosLinks, miniatureNews, promoNumbers)
    }

    private fun getPromoNumbers(elementsGroup: Element): PromoNumbers {
        val elementsWithoutWrapper = elementsGroup.getElementsByAttribute("data-percent")

        val numbers = elementsWithoutWrapper.map { element ->
            element.attr("data-percent").toInt()
        }

        return PromoNumbers(numbers[0], numbers[1], numbers[2], numbers[3])
    }

    /** Returns links for the [MainPageInfo.promoPhotosLinks]. */
    private fun getPromoPhotoLinks(elementsGroup: Element): List<String> {
        val elementsByAttribute = elementsGroup.getElementsByAttribute("src")
        return elementsByAttribute.map { element: Element ->
            element.attr("src")
        }

    }

    private fun getMiniatureNews(elementsGroup: Element): List<News> {
        fun getPhotoLink(element: Element): String? {
            val linkFromWebsite = element.getElementsByAttribute("src").attr("src")
            return if (linkFromWebsite.isNullOrEmpty()) null else linkFromWebsite
        }

        val items = elementsGroup.getElementsByClass("cmsmasters_owl_slider_item")
        return items.map { element ->
            // both title and full text info are inside this
            val elementsWithHref = element.getElementsByAttribute("href")

            val title = elementsWithHref.attr("title")
            val linkToFullText = elementsWithHref.attr("href")
            val linkToPhotoPreview = getPhotoLink(element)
            val dateOfCreation = element.getElementsByClass("published")[0].attr("title")

            News(
                title,
                null,
                1, // mini news are only on the first page
                linkToFullText,
                null,
                linkToPhotoPreview,
                dateOfCreation,
                null
            )
        }
    }

    private fun getElementsInColumns(doc: Document): List<Element> =
        doc.getElementsByClass("cmsmasters_column_inner")

    private fun getTitle(elementsGroup: Element): String =
        elementsGroup.getElementsByTag("h2").text()

    private fun getPromoText(elementsGroup: Element) = elementsGroup.getElementsByTag("h5").text()
}