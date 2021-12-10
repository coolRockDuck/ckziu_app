package com.ckziu_app.utils

import android.widget.ArrayAdapter
import android.widget.ListAdapter

/** Performs action on all elements of [ArrayAdapter]*/
fun <T> ArrayAdapter<T>.forEachElement(func: (T) -> (Unit)) {
    for (index in 0 until this.count) {
        func(this.getItem(index)!!)
    }
}

/** Performs action on all elements of [ListAdapter]*/
fun ListAdapter.forEachElement(func: (item: Any) -> (Unit)) {
    for (index in 0 until this.count) {
        func(this.getItem(index))
    }
}