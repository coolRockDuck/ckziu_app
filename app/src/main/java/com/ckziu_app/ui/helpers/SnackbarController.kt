package com.ckziu_app.ui.helpers

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ckziu_app.ui.activities.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** This class is responsible for showing new snackbar with error message
 * and hiding the old one when user has changed destination.*/
class SnackbarController(private var mainActivity: MainActivity?) {

    private var activeSnackbar: Snackbar? = null

    /** Launching last snackbar with delay.
     * When lastSnackbar is going to be hidden then this should be canceled.*/
    private var activeSnackbarDelayJob: Job? = null

    /** Presents error information to the user.
     *
     * When [lifecycleOwner] calls [Lifecycle.Event.ON_STOP] the snackbar will be destroyed.
     * @param lifecycleOwner Lifecycle of fragment associated with the snackbar.
     * @param errorMsg Error message displayed in the snackbar.
     * @param actionMsg Message used as clickable text which is activating actionClickListener
     * @param actionClickListener action which should be performed after actionMsg is clicked
     */

    fun showSnackbar(
        lifecycleOwner: LifecycleOwner,
        errorMsg: String,
        actionMsg: String?,
        actionClickListener: View.OnClickListener?
    ) {
        hideSnackbar() // hiding last active snackbar

        val observer = LifecycleEventObserver { _, event ->
            // snackbar should be hidden when user had navigated away from the destination associated with the the snackbar
            if (event == Lifecycle.Event.ON_STOP) hideSnackbar()
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        mainActivity?.run {
            activeSnackbarDelayJob = lifecycleScope.launch(Dispatchers.Main ) {

                delay(1_500)
                activeSnackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    errorMsg,
                    Snackbar.LENGTH_INDEFINITE
                ).apply {
                    setAnchorView(
                        com.example.ckziuapp.R.id.bottom_navbar
                    ).setAction(
                        actionMsg,
                        actionClickListener
                    )

                    show()
                }
            }
        }
    }

    internal fun hideSnackbar() {
        activeSnackbar?.setAction(null, null)
        activeSnackbar?.dismiss()
        activeSnackbarDelayJob?.cancel()
        activeSnackbar = null
    }

    /** Removing [mainActivity] reference to prevent memory leaks.*/
    internal fun destroySnackbarController() {
        hideSnackbar()
        activeSnackbarDelayJob?.cancel()
        mainActivity = null
    }
}