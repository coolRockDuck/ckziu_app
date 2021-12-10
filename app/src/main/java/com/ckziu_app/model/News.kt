package com.ckziu_app.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.jetbrains.annotations.TestOnly

/** Class that contains info about one news.
 * Only [title] is mandatory for creation of an instance of this class.
 * Most of the fields are nullable because some news,
 * especially old ones, don`t contain some attributes - for example photo or even text.
 * Also app cannot predict how future articles will look like.
 *
 * @param title Title of the news, every news **MUST** contain at least title.
 * @param textPreview Shorter version of text from [articleHtml].
 * Used f.g inside a card view in a recycle view.
 * @param linkToFullArticle Link which is used to load [articleHtml]
 * @param articleHtml Text that is loaded lazily. It should be loaded only when user has shown
 * explicit will of viewing it, fg. by clicking on the news card.
 * @param linkToImage Link that to the image which is loaded into card view inside [com.ckziu_app.ui.fragments.NewsListFragment]
 * and to the image view [com.ckziu_app.ui.fragments.NewsDetailsFragment]
 * @param dateOfCreation Date of creation of the news - fg. '15 marca 2018'
 * @param creatorName Name of the creator - fg. 'Tomasz Kowalski'
 *  */

@Entity
@Parcelize
data class News(
    @PrimaryKey(autoGenerate = true)
    val newsID: Int = 0,
    val title: String,
    val pageIndex: Int, // todo think about better system of grouping news
    val textPreview: String?,
    val linkToFullArticle: String?,
    var articleHtml: String?,
    val linkToImage: String?,
    val dateOfCreation: String?,
    val creatorName: String?,
) : Parcelable {

    companion object {
        const val NEWS_PARCELABLE_KEY = "NEWS_PARCELABLE_KEY"

        @TestOnly
        fun createEmptyNews() = News( "", "", 1, "", "", "", "", "")

    }

    constructor(
        title: String,
        textPreview: String?,
        pageIndex: Int,
        linkToFullText: String?,
        fullText: String?,
        linkToImage: String?,
        dateOfCreation: String?,
        creatorName: String?,
    ) : this(
        newsID = 0,
        title,
        pageIndex,
        textPreview,
        linkToFullText,
        fullText,
        linkToImage,
        dateOfCreation,
        creatorName,
    )

}