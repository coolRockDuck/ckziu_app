package com.ckziu_app.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ckziu_app.model.Lesson
import com.ckziu_app.model.ScheduleForDay
import com.ckziu_app.utils.forAllChildren
import com.ckziu_app.utils.getStyledColors
import com.ckziu_app.utils.makeGone
import com.ckziu_app.utils.makeVisible
import com.example.ckziuapp.R

/** Adapts list of lessons for displaying in time table*/
class LessonsViewAdapter(
    private val scheduleForWeek: List<ScheduleForDay>,
    private val lessonDetailsDisplay: LessonDetailsDisplay
) : RecyclerView.Adapter<LessonsViewAdapter.ScheduleAbstractViewHolder>() {

    companion object {
        private const val TAG = "LessonsViewAdapter"

        /** Key of the [ScheduleAbstractViewHolder.DayRowViewHolder] */
        private const val DAY_NAME_CELL_VIEW = 0

        /** Key of the [ScheduleAbstractViewHolder.LessonsRowViewHolder] */
        private const val LESSON_CELL_VIEW = 1
    }

    /** Interface for displaying details about lesson */
    interface LessonDetailsDisplay {
        fun showDetailsAbout(lesson: Lesson)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) DAY_NAME_CELL_VIEW else LESSON_CELL_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAbstractViewHolder {
        fun inflate(viewId: Int): View {
            return LayoutInflater.from(parent.context).inflate(
                viewId,
                parent,
                false
            )
        }

        return when (viewType) {
            DAY_NAME_CELL_VIEW -> ScheduleAbstractViewHolder.DayRowViewHolder(inflate(R.layout.day_row_layout))
            LESSON_CELL_VIEW -> ScheduleAbstractViewHolder.LessonsRowViewHolder(
                (inflate(R.layout.lesson_row) as LinearLayout),
                lessonDetailsDisplay
            )
            else -> {
                Log.e(TAG, "Unsupported view type = $viewType")
                ScheduleAbstractViewHolder.LessonsRowViewHolder(
                    (inflate(R.layout.day_row_layout) as LinearLayout),
                    lessonDetailsDisplay
                )
            }
        }
    }


    override fun onBindViewHolder(holder: ScheduleAbstractViewHolder, position: Int) {

        when (holder) {
            is ScheduleAbstractViewHolder.DayRowViewHolder -> {
                holder.bindDayNames()
            }

            is ScheduleAbstractViewHolder.LessonsRowViewHolder -> {
                // first position contains names of the days
                val indexOfLesson = position - 1
                val rowOfLessons = scheduleForWeek.map { scheduleForDay ->
                    scheduleForDay.lessons[indexOfLesson]
                }


                holder.bindLessons(position, rowOfLessons)
            }
        }
    }

    // TODO: 19.03.21 add minimal size of table
    override fun getItemCount(): Int = scheduleForWeek.maxOf { it.lessons.size + 1 }

    /** Abstract view holder.
     * @param localItemView view of the item passed as
     * the parameter to the [RecyclerView.ViewHolder]*/
    sealed class ScheduleAbstractViewHolder(val localItemView: View) :
        RecyclerView.ViewHolder(localItemView) {

        /** View holder designed for displaying lessons
         * @param rowView linear layout holding row of cells
         * @param detailsDisplay implementation of [LessonDetailsDisplay]
         * which will display more information after taping by the user */
        class LessonsRowViewHolder(
            rowView: LinearLayout,
            private val detailsDisplay: LessonDetailsDisplay
        ) : ScheduleAbstractViewHolder(rowView) {

            companion object {
                const val TAG = "LessonsViewHolder"
            }

            private val rowOfLessonsViews = getCellsForRow(rowView)

            fun bindLessons(position: Int, lessonsForRow: List<Lesson>?) {

                setBackgroundColorOfCellsAndRows(position)

                if (lessonsForRow == null) {
                    Log.e(TAG, "Lesson for this row are null")
                }

                lessonsForRow?.forEachIndexed { index: Int, lesson: Lesson ->
                    val cellOfLesson = rowOfLessonsViews[index]
                    val mainTvOfCell = cellOfLesson.findViewById<TextView>(R.id.tv_lesson_main_text)
                    val secondaryTvOfCell =
                        cellOfLesson.findViewById<TextView>(R.id.tv_lesson_secondary_text)

                    cellOfLesson.setOnClickListener {
                        if (!lesson.subjects.isNullOrEmpty()) {
                            detailsDisplay.showDetailsAbout(lesson)

                        }
                    }

                    when (lesson.subjects.size) {
                        0 -> {
                            mainTvOfCell.makeGone()
                            secondaryTvOfCell.makeVisible()
                        }

                        1 -> {
                            mainTvOfCell.text = lesson.subjects[0]
                            secondaryTvOfCell.makeVisible()
                        }

                        2 -> {
                            mainTvOfCell.text = lesson.subjects[0]
                            secondaryTvOfCell.text = lesson.subjects[1]
                        }

                        else -> {
                            Log.w(
                                TAG,
                                "bind: Unsupported number of lessons at one time = ${lesson.subjects.size}, lesson = $lesson"
                            )
                        }
                    }
                }
            }

            private fun getCellColors(): List<Int> {
                val colorsIds = listOf(
                    R.attr.lessonCellColorBrightest,
                    R.attr.lessonCellColorBright,
                    R.attr.lessonCellColorDark,
                    R.attr.lessonCellColorDarkest
                ).toIntArray()

                return localItemView.context.getStyledColors(*colorsIds)
            }

            private fun setBackgroundColorOfCellsAndRows(columnIndex: Int) {
                val cellColors = getCellColors()

                localItemView.forAllChildren { view, cellIndex ->
                    // makes 'chess board style' pattern of colors
                    var darknessLevel = 0
                    if (cellIndex % 2 == 0) {
                        darknessLevel++
                    }

                    if (columnIndex % 2 == 0) {
                        darknessLevel++
                    }

                    view.setBackgroundColor(cellColors[darknessLevel])
                }
            }

            private fun getCellsForRow(dayRowView: View): List<ConstraintLayout> {

                val idOfCells = listOf(
                    R.id.lesson_cell_one,
                    R.id.lesson_cell_two,
                    R.id.lesson_cell_three,
                    R.id.lesson_cell_four,
                    R.id.lesson_cell_five,
                )

                return idOfCells.map { id ->
                    dayRowView.findViewById<ConstraintLayout>(id)
                }
            }
        }

        /** View holder designed for displaying days of week.*/
        class DayRowViewHolder(itemView: View) : ScheduleAbstractViewHolder(itemView) {
            fun bindDayNames() {
                val dayNameIdList = listOf(
                    R.id.cell_day_name_1,
                    R.id.cell_day_name_2,
                    R.id.cell_day_name_3,
                    R.id.cell_day_name_4,
                    R.id.cell_day_name_5,
                )

                val dayNameViewList = dayNameIdList.map { id ->
                    itemView.findViewById<ConstraintLayout>(id)
                        .findViewById<TextView>(R.id.tv_lesson_main_text)
                }

                val res = itemView.resources
                dayNameViewList.forEachIndexed { index, textView ->
                    textView.text = when (index) {
                        0 -> res.getString(R.string.day_monday)
                        1 -> res.getString(R.string.day_tuesday)
                        2 -> res.getString(R.string.day_wednesday)
                        3 -> res.getString(R.string.day_thursday)
                        4 -> res.getString(R.string.day_friday)
                        else -> {
                            Log.e(LessonsRowViewHolder.TAG, "Unsupported day of the week")
                            ""
                        }
                    }
                }
            }
        }
    }
}
