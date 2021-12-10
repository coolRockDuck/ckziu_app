package com.ckziu_app.ui.helpers

import android.view.View
import androidx.lifecycle.LifecycleOwner

/** Interface designed for requesting activity, to inform user about an error, coming from a fragment .
 * @see showErrorSnackbar
 * @see hideErrorSnackbar
 *  */

interface ErrorInformant {
    /** Displays error message to an user.
     * @param lifecycleOwner Lifecycle owner connected to a snackbar.
     * @param errorMsg Message displayed to an user.
     * @param actionMsg Message activating the actionClickListener.
     * @param actionClickListener Action which will be activated after actionMsg has been clicked.
     * */
    fun showErrorSnackbar(
        lifecycleOwner: LifecycleOwner,
        errorMsg: String,
        actionMsg: String?,
        actionClickListener: View.OnClickListener?
    )

    /** Hides error message from an user. */
    fun hideErrorSnackbar()
}
