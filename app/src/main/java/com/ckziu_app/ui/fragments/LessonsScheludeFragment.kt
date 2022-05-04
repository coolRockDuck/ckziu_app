package com.ckziu_app.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ckziu_app.di.RepositoryProvider
import com.ckziu_app.model.*
import com.ckziu_app.ui.adapters.LessonsViewAdapter
import com.ckziu_app.ui.helpers.ErrorInformant
import com.ckziu_app.ui.helpers.ScrollControllerInterface
import com.ckziu_app.ui.viewmodels.*
import com.ckziu_app.ui.viewmodels.factories.LessonScheduleViewModelFactory
import com.ckziu_app.utils.hideKeyboard
import com.ckziu_app.utils.makeGone
import com.ckziu_app.utils.makeVisible
import com.example.ckziuapp.R
import com.example.ckziuapp.databinding.FragmentLessonsScheduleBinding
import kotlinx.coroutines.Dispatchers

/** Fragment presenting time table consisting of multiple [com.ckziu_app.model.News] of chosen target.
 * @see com.ckziu_app.ui.activities.MainActivity*/
class LessonsScheduleFragment :
    Fragment(R.layout.fragment_lessons_schedule),
    ScrollControllerInterface,
    LessonsViewAdapter.LessonDetailsDisplay {

    companion object {
        const val PREFERENCES_TARGET_NAME_KEY = "PREFERENCES_TARGET_NAME_KEY"
        const val PREFERENCES_TARGET_TYPE_KEY = "PREFERENCES_TARGET_TYPE_KEY"
    }

    private lateinit var errorInformant: ErrorInformant
    private var spinnerController: SpinnersController? = null

    private var _viewBinding: FragmentLessonsScheduleBinding? = null
    val viewBinding get() = _viewBinding!!

    private val viewModel by activityViewModels<LessonsScheduleViewModel> {
        LessonScheduleViewModelFactory(
            (requireContext().applicationContext as RepositoryProvider).getScheduleRepo(),
            Dispatchers.IO
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when {
            context.applicationContext !is RepositoryProvider -> {
                throw IllegalStateException("Application needs to implement RepositoryProvider")
            }

            context !is ErrorInformant -> {
                throw IllegalStateException("Activity needs to implement ErrorInformant")
            }

            else -> {
                errorInformant = context
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = FragmentLessonsScheduleBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservable()
    }

    private fun setObservable() {
        fun showLessonScheduleError() {
            hideProgressbar()
            val errMsg = resources.getString(R.string.no_interent_day_schedule)
            val tryAgainMsg = resources.getString(R.string.tap_to_try_again)
            val tryUpdateLessons = View.OnClickListener {
                viewBinding.atvLessonsScheduleTargetSearcher.let { view ->
                    viewModel.updateTargetsAndSchedule(view.text.toString())
                }
            }

            errorInformant.showErrorSnackbar(this, errMsg, tryAgainMsg, tryUpdateLessons)
        }


        viewModel.scheduleForWeek.observe(viewLifecycleOwner) { scheduleForWeekResult ->
            when (scheduleForWeekResult) {
                is Success -> populatePlan(scheduleForWeekResult.resultValue)
                is InProgress -> showProgressBar()
                is Failure -> showLessonScheduleError()
            }
        }

        viewModel.listOfScheduleTargets.observe(viewLifecycleOwner) { namesOfTargetsResult ->
            when (namesOfTargetsResult) {
                is Success -> {
                    spinnerController?.run {
                        listOfTargets = namesOfTargetsResult.resultValue
                    } ?: run {
                        val savedInfo = getSavedTargetInfo()
                        spinnerController = SpinnersController(
                            this,
                            namesOfTargetsResult.resultValue,
                            savedInfo.first,
                            savedInfo.second
                        )
                    }
                }

                is InProgress -> showProgressBar()

                is Failure -> {
                    showLessonScheduleError()
                }
            }
        }

    }

    internal fun changeTarget(newTargetName: String) {
        viewModel.collectLessonsSchedule(newTargetName)
    }


    private fun populatePlan(scheduleForWeek: List<ScheduleForDay>) {
        hideProgressbar()
        requireActivity().hideKeyboard()

        viewBinding.rvLessonsSchedule.run {
            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )

            adapter = LessonsViewAdapter(scheduleForWeek, this@LessonsScheduleFragment)
        }
    }

    /** Displays details about [Lesson]*/
    override fun showDetailsAbout(lesson: Lesson) {
        val ctx = requireActivity()
        val fragMan = ctx.supportFragmentManager
        val parentTag = tag

        LessonDetailsBottomSheet().run {
            arguments = Bundle().apply {
                putParcelable(LessonDetailsBottomSheet.LESSON_ARG, lesson)
            }

            show(fragMan, parentTag)
        }
    }

    override fun onPause() {
        super.onPause()
        saveSearchTarget()
        spinnerController?.destroy()
    }

    /** Saves target`s name. */
    private fun saveSearchTarget() {
        val textFromInput = viewBinding.atvLessonsScheduleTargetSearcher.text

        // if name of the target it empty then save first name of chosen type
        val nameOfTargetToSave = if (textFromInput.isNullOrEmpty()) {
            when (val result = viewModel.listOfScheduleTargets.value) {
                is Success -> result.resultValue.groupNames[0]
                else -> null
            }
        } else {
            textFromInput.toString()
        }

        val defValue = SpinnersController.DEFAULT_PERSPECTIVE.ordinal
        val perspective = spinnerController?.targetType?.ordinal ?: defValue

        requireActivity().getPreferences(Context.MODE_PRIVATE).edit().run {
            putInt(PREFERENCES_TARGET_TYPE_KEY, perspective)
            putString(PREFERENCES_TARGET_NAME_KEY, nameOfTargetToSave)
            apply()
        }
    }

    private fun getSavedTargetInfo(): Pair<Type, String> {
        requireActivity().getPreferences(Context.MODE_PRIVATE).run {
            val savedName = getString(PREFERENCES_TARGET_NAME_KEY, null) ?: ""
            val defValue = Type.GROUP_NAMES.ordinal
            val savedTypeOrdinal = getInt(PREFERENCES_TARGET_TYPE_KEY, defValue)
            val type = Type.values()[savedTypeOrdinal]
            return type to savedName
        }
    }

    private fun showProgressBar() {
        viewBinding.loadingLessonsSchedulePb.makeVisible()
    }

    private fun hideProgressbar() {
        viewBinding.loadingLessonsSchedulePb.makeGone()
    }

    /** Implementing [ScrollControllerInterface.scrollToTheTop]. */
    override fun scrollToTheTop() {
        viewBinding.run {
            rvLessonsSchedule.smoothScrollToPosition(0)
            svHorizontalLessonschedule.smoothScrollTo(0, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}