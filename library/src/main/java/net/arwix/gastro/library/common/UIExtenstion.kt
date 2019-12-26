package net.arwix.gastro.library.common

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

fun NavDirections.navigate(fragment: Fragment) {
    findNavController(fragment).navigate(this)
}

fun Fragment.setToolbarTitle(title: CharSequence?) {
    (requireActivity() as AppCompatActivity).supportActionBar?.apply {
        this.title = title
    }
}

fun AppBarLayout.asCollapsedFlow(init: Boolean) =
    callbackFlow {
        offer(init)
        val listener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                offer(true)
                //collapsed
            } else {
                offer(false)
            }
        }
        this@asCollapsedFlow.addOnOffsetChangedListener(listener)

        awaitClose {
            cancel()
            Log.e("setCollapsedListener", "remove")
            this@asCollapsedFlow.removeOnOffsetChangedListener(listener)
        }
    }.distinctUntilChanged()