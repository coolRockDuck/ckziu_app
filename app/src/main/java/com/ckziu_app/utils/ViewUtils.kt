package com.ckziu_app.utils

import android.view.View
import android.view.ViewGroup

/** Iterate over views children and perform given action on them. */
fun View.forAllChildren(action: (View, Int) -> Unit) {
    val viewGroup = this as ViewGroup
    for (childIndex: Int in 0 until (viewGroup.childCount)) {
        action(viewGroup.getChildAt(childIndex), childIndex)
    }
}

/** Setting visibility of view to [View.VISIBLE].*/
fun View?.makeVisible() {
    this?.visibility = View.VISIBLE
}

/** Setting visibility of view to [View.GONE].*/
fun View?.makeGone() {
    this?.visibility = View.GONE
}