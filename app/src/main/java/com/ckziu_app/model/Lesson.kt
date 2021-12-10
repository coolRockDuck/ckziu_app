package com.ckziu_app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Class that holds info about one lesson field.
 *  Firstly check [schedule of lessons](http://ckziu.olawa.pl/planlekcji/index.html) for better understanding.
 *
 * Many subgroups can participate in one lesson, so it could contain multiple teachers, classrooms etc.
 *  @param subjects List of subjects names. Most of the time this list contains one, but in some cases, two elements.
 * It`s occurring only when in one time frame target has [splitted][isSplitted] lessons
 * fg. when one group has P.E and second one has Graphical Design.
 * @param index index of lesson in lessons schedule
 * @param timeSpan time of begging and end of the lesson
 * @param groupsNames names of groups of students fg. - 4C
 * @param teachersNames names of teachers
 * @param classRooms list of classrooms*/

@Parcelize
data class Lesson(
    val index: Int,
    val subjects: List<String>,     // Pairs are not used because they can only hold two elements
    val timeSpan: String,           // and if more than 2 groups will be used in the future then
    val groupsNames: List<String>,  // it would necessary to rework a lot of code.
    val teachersNames: List<String>,
    val classRooms: List<String>,
) : Parcelable {
    /** Returns true if more than one subgroups participates in the lesson.*/
    fun isSplitted() = subjects.size >= 2
    /** [com.ckziu_app.ui.fragments.LessonDetailsBottomSheet.populateView] relays strongly on this */
}

/** Wrapper class for holding lessons for one day.*/
data class ScheduleForDay(val lessons: MutableList<Lesson> = mutableListOf())