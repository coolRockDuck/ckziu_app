package com.ckziu_app.ui.helpers

/**  Interface created for communication,
 *   about scrolling, between an activity and fragments inside of it.
 *   */
interface ScrollControllerInterface {
    /** When user tries to navigates to the destination they are already in,
     *  then main list of active fragment should scroll to the top.
     *
     *  fg. - when user taps on the destination icon they are already in.*/
    fun scrollToTheTop()
}