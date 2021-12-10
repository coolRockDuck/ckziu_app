package com.ckziu_app.ui.fragments

import android.util.Log
import android.widget.ArrayAdapter
import com.ckziu_app.data.network.LessonScheduleGetter
import com.ckziu_app.model.NamesOfTargets
import com.example.ckziuapp.R
import com.example.ckziuapp.databinding.FragmentLessonsScheduleBinding

/** Class communicating between
 * [type spinner][FragmentLessonsScheduleBinding.atvTargetTypeChooser],
 * [name spinner][FragmentLessonsScheduleBinding.tlLessonsScheduleTargetSearcher]
 * and [LessonsScheduleFragment].
 * When type of target changes in type chooser then different targets
 * are available inside name spinner.
 *
 * When name is chosen then LessonsScheduleFragment is informed and schedule is searched for the target with the given name.
 *
 * @param lessonFragment fragment for displaying lessons
 * @param listOfTargets names of possible targets of plan
 * @param savedType initial type for search
 * @param savedName initial name for search
 * */

internal class SpinnersController(
    private var lessonFragment: LessonsScheduleFragment?,
    internal var listOfTargets: NamesOfTargets,
    savedType: Type = Type.GROUP_NAMES,
    savedName: String = "",
) {

    companion object {
        const val TAG = "SpinnersController"

        val DEFAULT_PERSPECTIVE = LessonScheduleGetter.PerspectiveMode.GROUP

        private val listOfID = listOf(
            R.string.class_of_studnets,
            R.string.teacher,
            R.string.classroom
        )
    }

    private var viewBinding: FragmentLessonsScheduleBinding? = lessonFragment?.viewBinding

    var targetType: Type = Type.GROUP_NAMES
        set(type) {
            field = type
            val listOfProperType = getListOfProperType()
            setArrayAdapter(listOfProperType)
        }


    init {
        populate(savedType, savedName)
        setListeners()
        initialSearch()
    }

    private fun getListOfProperType(): List<String> {
        return when (targetType) {
            Type.GROUP_NAMES -> {
                listOfTargets.groupNames
            }
            Type.TEACHERS_NAMES -> {
                listOfTargets.teachersNames
            }
            Type.CLASSROOM_NAMES -> {
                listOfTargets.classroomsNames
            }
        }
    }

    private fun setListeners() {
        viewBinding?.run {
            atvTargetTypeChooser.setOnItemClickListener { _, _, position, _ ->
                targetType = when (listOfID[position]) {
                    R.string.class_of_studnets -> {
                        Type.GROUP_NAMES
                    }

                    R.string.classroom -> {
                        Type.CLASSROOM_NAMES
                    }

                    R.string.teacher -> {
                        Type.TEACHERS_NAMES
                    }

                    else -> throw UnsupportedOperationException("")
                }

                atvLessonsScheduleTargetSearcher.setOnItemClickListener { adapterView, _, position, _ ->
                    val selectedItem = adapterView.adapter.getItem(position) as String
                    lessonFragment?.changeTarget(selectedItem)
                }
            }
        }
    }

    /**  Launches initial search for saved targetName if it`s valid
     *  or start searching for the first name from the list of targets */
    private fun initialSearch() {
        viewBinding?.run {
            val adapter = atvLessonsScheduleTargetSearcher.adapter
            var containsValidTargetName = false
            for (index in 0 until adapter.count) {
                val item = adapter.getItem(index)
                if (item == atvLessonsScheduleTargetSearcher.text.toString()) {
                    lessonFragment?.changeTarget(item.toString())
                    containsValidTargetName = true
                    break
                }
            }

            if (!containsValidTargetName) { // if targetName is NOT valid then search for first item
                lessonFragment?.changeTarget(adapter.getItem(0).toString())
            }
        }
    }


    private fun populate(type: Type = Type.GROUP_NAMES, targetName: String = "") {

        lessonFragment?.run {
            viewBinding.run {
                val typesOfTargets = listOfID.map { id -> lessonFragment?.resources?.getString(id) }

                targetType = type
                val targetTypeName = typesOfTargets[type.ordinal]
                atvTargetTypeChooser.setText(targetTypeName)

                ArrayAdapter<String>(
                    requireContext(), R.layout.spinner_item_layout, typesOfTargets
                ).let { adapter ->
                    atvTargetTypeChooser.setAdapter(adapter)
                }

                atvLessonsScheduleTargetSearcher.setText(targetName)
            }

            setArrayAdapter(getListOfProperType())
        }
    }

    private fun setArrayAdapter(listOfProperType: List<String>) {
        lessonFragment?.run {
            val arrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.spinner_item_layout,
                listOfProperType
            )

            viewBinding.atvLessonsScheduleTargetSearcher.setAdapter(arrayAdapter)
        }
    }

    /** Removing [lessonFragment] reference to prevent memory leaks*/
    internal fun destroy() {
        Log.d(TAG, "Destroying spinner controller")
        lessonFragment = null
        viewBinding = null
    }
}

enum class Type {
    GROUP_NAMES, TEACHERS_NAMES, CLASSROOM_NAMES;
}