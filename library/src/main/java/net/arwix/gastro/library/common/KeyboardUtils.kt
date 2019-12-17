package net.arwix.gastro.library.common

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import net.arwix.extension.runWeak
import net.arwix.extension.weak


fun Activity.hideKeyboard() {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
    val view = currentFocus ?: View(this)
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.showSoftKeyboard() {
    val view = requireActivity().currentFocus
    val imm =
        requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
    if (view != null && imm != null) {
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}


inline fun Fragment.handleOnBackPressed(crossinline block: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            block()
            this.remove()
        }
    })
}

fun Fragment.hideKeyboardOnBackPressed() {
    val weak = requireActivity().weak()
    handleOnBackPressed {
        weak.runWeak { hideKeyboard() }
    }
}