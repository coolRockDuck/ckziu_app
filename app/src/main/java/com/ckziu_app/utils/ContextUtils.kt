package com.ckziu_app.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.core.content.res.getColorOrThrow

/** **Extension function** for [Context] which returns list of colors styled by context **on** which the function was called.*/
fun Context.getStyledColors(@AttrRes vararg colorsIds: Int): List<Int> {
    theme.obtainStyledAttributes(colorsIds.toList().toIntArray()).let { colors ->
        return List(colors.indexCount) { index ->
            try {
                colors.getColorOrThrow(index)
            } catch (e: Exception) {
                Log.e("getStyledColors", "Color with index: $index is not defined in the theme.")
            }
        }
    }
}

fun Activity.hideKeyboard() {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val focusView = currentFocus ?: View(this)
    inputManager.hideSoftInputFromWindow(focusView.windowToken, 0)
}