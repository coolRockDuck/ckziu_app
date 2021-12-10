package com.ckziu_app.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ckziu_app.model.Lesson
import com.ckziu_app.ui.fragments.LessonDetailsBottomSheet.Companion.LESSON_ARG
import com.ckziu_app.utils.makeVisible
import com.example.ckziuapp.R
import com.example.ckziuapp.databinding.FragmentLessonDetailsBinding
import com.example.ckziuapp.databinding.FragmentLessonGroupInfoDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/** Fragment which displays more information about chosen [Lesson].
 * Lesson which details will be shown must be passed
 * as an [argument][BottomSheetDialogFragment.requireArguments]
 * associated with the key [LESSON_ARG].
 *
 * @property lesson Lesson passed as an argument */

class LessonDetailsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var lesson: Lesson

    companion object {
        const val TAG = "LessonBottomSheet"
        const val LESSON_ARG = "LESSON_ARG"
    }

    private var _viewBinding: FragmentLessonDetailsBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().getParcelable<Lesson>(LESSON_ARG).let { argLesson ->
            if (argLesson == null) {
                Log.e(TAG, "onCreate: No lesson in arguments")
                throw IllegalArgumentException("Arguments do NOT contains any lesson info")
            }

            lesson = argLesson
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = FragmentLessonDetailsBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateView()
    }

    /** Populating bottom sheet with details about chosen [Lesson] */
    private fun populateView() {
        /** each group should have each own segment of details,
        but for now only max two groups are being used inside timetable on school`s website
        see docs of [Lesson] and [com.ckziu_app.data.network.LessonScheduleGetter] for more details */
        viewBinding.run {
            if (lesson.isSplitted()) {
                fcSecondGroup.root.makeVisible()
                lessonDetailsDivider.makeVisible()

                populateGroupDetails(fcFirstGroup, 0)
                populateGroupDetails(fcSecondGroup, 1)
            } else {
                populateGroupDetails(fcFirstGroup, 0)
            }
        }
    }

    /** Populating group details view with information.
     * @param groupDetails view of the group - [R.layout.fragment_lesson_group_info_details]
     * @param groupIndex index of the group */
    private fun populateGroupDetails(
        groupDetails: FragmentLessonGroupInfoDetailsBinding,
        groupIndex: Int
    ) {
        // todo using perspective mode for displaying info
        groupDetails.run {
            lesson.run {
                tvLessonDetailsSubjectName.text = getTargetDetailOrNull(subjects, groupIndex)
                tvLessonDetailsIndexNumber.text = index.toString()
                tvLessonDetailsTimeInfo.text = timeSpan
                tvLessonDetailsGroupName.text = getTargetDetailOrNull(groupsNames, groupIndex)
                tvLessonDetailsTeacherName.text = getTargetDetailOrNull(teachersNames, groupIndex)
                tvLessonDetailsClassroomName.text = getTargetDetailOrNull(classRooms, groupIndex)
            }
        }
    }


    /** Helper function designed to help with nullability of some properties of [Lesson].
     *  Also helps with lessons having the same info for more than one group, eg - the same teacher or classroom*/
    private fun <T> getTargetDetailOrNull(list: List<T>, groupIndex: Int): T? {
        return when (list.size) {
            0 -> {
                Log.e(TAG, "getTargetDetailOrNull: empty list = $list, in lesson = $lesson")
                null
            }
            1, 2 -> if (groupIndex > list.lastIndex) list.last() else list[groupIndex]
            else -> {
                Log.w(TAG, "getTargetDetailOrNull: too long list = $list, in lesson = $lesson")
                if (groupIndex > list.lastIndex) null else list[groupIndex]
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}