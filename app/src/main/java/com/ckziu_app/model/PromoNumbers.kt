package com.ckziu_app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class holding numbers of which the school is proud.
 *
 * @param amountOfBestStudents number of students which have taken part in some competition
 * @param amountOfExperienceYrs number of years the school exists.
 */
@Parcelize
data class PromoNumbers(
    val amountOfStudents: Int,
    val amountOfBestStudents: Int, // todo better name for this
    val amountOfTeachers: Int,
    val amountOfExperienceYrs: Int
) : Parcelable {
    companion object {
        fun emptyPromoNumbers() = PromoNumbers(1, 1, 1, 1)
    }
}
