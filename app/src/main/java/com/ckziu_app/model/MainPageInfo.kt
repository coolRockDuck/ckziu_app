package com.ckziu_app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.jetbrains.annotations.TestOnly

/** Wrapper class for holding info about main page.
 * @param title Title of page.
 * @param promoText Text promoting school.
 * @param promoPhotosLinks Links to photos promoting school.
 * @param miniListOfNews List of news from bottom of main page.
 * @param promoNumbers Numbers promoting school, fg. number of teachers, how long school exists. */
@Parcelize
data class MainPageInfo(
    val title: String,
    val promoText: String,
    val promoPhotosLinks: List<String>,
    val miniListOfNews: List<News>,
    val promoNumbers: PromoNumbers
) : Parcelable {
    companion object {
        @TestOnly
        fun emptyMainPageInfo() = MainPageInfo("", "", emptyList(), emptyList(), PromoNumbers.emptyPromoNumbers())
    }
}