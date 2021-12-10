package com.ckziu_app.model

import com.ckziu_app.ui.fragments.SpinnersController

/** Wrapper class for holding names for possible targets of search of lessons schedule.
 * Used inside [SpinnersController] */

data class NamesOfTargets(
    val groupNames: List<String>,
    val teachersNames: List<String>,
    val classroomsNames: List<String>
)