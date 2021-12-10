@file:Suppress("ConvertToStringTemplate", "ConvertToStringTemplate")

package com.ckziu_app.data.network

import android.util.Log
import com.ckziu_app.model.NamesOfTargets
import com.ckziu_app.model.Lesson
import com.ckziu_app.model.ScheduleForDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

/** Class which fetches information about lessons schedule.*/
class LessonScheduleGetter() {
    companion object {
        private const val baseLink: String = "http://ckziu.olawa.pl/planlekcji"
        private const val TAG = "LessonScheduleGetter"
        private const val TIMEOUT = 7_500
    }

    /** Main header which contains name of the target of the plan.*/
    private lateinit var mainHeader: String

    /** Time when each row lesson from particular row starts and ends.*/
    private val lessonsTimeSpans = mutableListOf<String>()

    /** Active OR last used [PerspectiveMode].*/
    private lateinit var perspectiveMode: PerspectiveMode

    /** Different modes of searching.
     * When looking fg. for teachers schedule on the [website][baseLink]
     * then **lessons cells do not contain teachers name** because it's obvious.
     * Likewise targetName from [getTargetSchedule] need to be used to get info
     * because there is no info about it in the lesson cells.
     *  - [Lesson.groupsNames] when [PerspectiveMode] is equal to [PerspectiveMode.GROUP]
     *  - [Lesson.teachersNames] when [PerspectiveMode] is equal to [PerspectiveMode.TEACHER]
     *  - [Lesson.classRooms] when [PerspectiveMode] is equal to [PerspectiveMode.CLASSROOM]
     **/
    enum class PerspectiveMode {
        GROUP, TEACHER, CLASSROOM
    }


    /** Returns schedule of target with given name.*/
    suspend fun getTargetSchedule(
        targetName: String,
        listOfTargets: NamesOfTargets? = null
    ): List<ScheduleForDay>? {
        return withContext(Dispatchers.IO) {
            return@withContext try {

                val namesOfTargets = listOfTargets ?: getTargetsNames() ?: return@withContext null

                changePerspectiveMode(targetName, namesOfTargets)

                val mainSite = Jsoup.connect(baseLink + "/" + "lista.html").timeout(TIMEOUT).get()
                val tabElements = mainSite.getElementsByTag("a")

                val targetLink = getTargetLink(tabElements, targetName)
                val targetWebsite = Jsoup.connect(targetLink).timeout(TIMEOUT).get()

                mainHeader = targetName

                getLessons(targetWebsite)

            } catch (e: Exception) {
                val error = IOException("Error occurred when loading lesson schedule : $e")
                Log.w(TAG, "getTargetSchedule: $error")
                return@withContext null
            }
        }
    }

    private fun getLessons(targetsWebsite: Document): List<ScheduleForDay> {
        val table = targetsWebsite.getElementsByClass("tabela")[0]
        val rows = table.getElementsByTag("tr").drop(1) // first row is header

        val group = mutableListOf<ScheduleForDay>()
        val numOfDaysInSchedule =
            rows[0].getElementsByClass("l").size // every row have the same number size

        for (i in 0 until numOfDaysInSchedule) {
            group.add(ScheduleForDay())
        }

        rows.forEach { row ->
            parseRow(row, group)
        }

        return group.toList() // making list un-mutable
    }

    private fun changePerspectiveMode(targetName: String, namesOfTargets: NamesOfTargets) {
        perspectiveMode = when (targetName) {
            in namesOfTargets.groupNames -> PerspectiveMode.GROUP
            in namesOfTargets.teachersNames -> PerspectiveMode.TEACHER
            in namesOfTargets.classroomsNames -> PerspectiveMode.CLASSROOM
            else -> {
                Log.e(
                    TAG,
                    "changePerspectiveMode: Any of the groups do NOT contain targetName = $targetName"
                )

                perspectiveMode // not changing perspectiveMode
            }
        }
    }


    private fun parseRow(row: Element, group: MutableList<ScheduleForDay>) {
        group.forEachIndexed { index: Int, day: ScheduleForDay ->
            day.lessons.add(parseLesson(row, index))
        }
    }

    /** Returns lesson from [row] and with index of [indexOfDayOfWeek].*/
    private fun parseLesson(row: Element, indexOfDayOfWeek: Int): Lesson {

        val lessonsEle = row.getElementsByClass("l") // removing another headers
        val lessonCell = lessonsEle[indexOfDayOfWeek]

        val index = getIndex(row)
        return Lesson(
            index,
            getSubject(lessonCell),
            getLessonTimeSpan(row, index),
            getGroupsNames(lessonCell),
            getTeachersNames(lessonCell),
            getClassroomNames(lessonCell),
        )
    }

    private fun getIndex(row: Element): Int {
        return Integer.parseInt(row.getElementsByClass("nr").text())
    }

    private fun getSubject(lessonCell: Element): List<String> {
        return lessonCell.getElementsByClass("p").eachText()
    }

    private fun getGroupsNames(lessonCell: Element): List<String> {
        if (perspectiveMode == PerspectiveMode.GROUP) {
            return listOf(mainHeader)
        }
        return lessonCell.getElementsByClass("o").eachText()
    }

    private fun getClassroomNames(lessonCell: Element): List<String> {
        if (perspectiveMode == PerspectiveMode.CLASSROOM) {
            return listOf(mainHeader)
        }
        return lessonCell.getElementsByClass("s").eachText()
    }

    private fun getTeachersNames(row: Element): List<String> {
        if (perspectiveMode == PerspectiveMode.TEACHER) {
            return listOf(mainHeader)
        }
        return row.getElementsByClass("n").eachText()
    }

    private fun getTargetLink(elements: Elements, groupName: String): String? {
        val smallLink = elements.getLinksEnd(groupName) ?: return null
        return baseLink + "/" + smallLink
    }

    private fun getLessonTimeSpan(row: Element, rowIndex: Int): String {
        val indexOfTimeSpan = rowIndex - 1
        /** starts from 0. [rowIndex] is equal to [Lesson.index] and starts form  1 */
        return if (lessonsTimeSpans.lastIndex >= indexOfTimeSpan) {
            lessonsTimeSpans[indexOfTimeSpan] // returning
        } else {
            row.getElementsByTag("tr")[0].let { rowWithoutWrapper ->
                val timeSpan = rowWithoutWrapper.getElementsByClass("g")[0].text()
                lessonsTimeSpans.add(timeSpan)
                timeSpan // returning
            }
        }
    }

    /** Returns [NamesOfTargets] */
    suspend fun getTargetsNames(): NamesOfTargets? {
        val groupsNames = mutableListOf<String>()
        val teachersNames = mutableListOf<String>()
        val classroomsNames = mutableListOf<String>()

        return withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(baseLink + "/" + "lista.html").timeout(7500).get()
                val docWithouthHeaders = doc.getElementsByTag("body").first()

                var properList = groupsNames
                docWithouthHeaders.allElements.forEach { ele ->
                    when (ele.tagName()) {

                        // switching group of the links
                        "h4" -> properList = when (ele.text()) {
                            "Oddziały" -> groupsNames

                            "Nauczyciele" -> teachersNames

                            "Sale" -> classroomsNames

                            else -> {
                                val error =
                                    UnsupportedOperationException("Unsupported type of links")
                                Log.w(TAG, "getLessonGroupsNames: ", error)
                                throw error
                            }
                        }

                        "a" -> properList.add(ele.text()) // adding link to proper group
                    }
                }

                NamesOfTargets(groupsNames, teachersNames, classroomsNames)
            } catch (e: Exception) {
                null
            }
        }
    }

    /** Returns trailing part of the **full** link, by searching in a group of elements
     *  for given name and link associated with it.
     *
     * Returns only end part of the link because elements on this page don't contain full links.
     * */
    private fun Elements.getLinksEnd(nameOfTarget: String): String? {
        for (ele in this) {
            val e = ele.getElementsByAttribute("href")
            if (e.text() == nameOfTarget) {
                val link = e.attr("href")
                return link
            }
        }

        return null
    }
}