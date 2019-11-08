package net.arwix.gastro.boss.common

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController

fun Toolbar.setupToolbar(
    navController: NavController,
    showHome: Boolean = true,
    homeAsUp: Boolean = true,
    navigationOnClickListener: View.OnClickListener? = null

): NavController.OnDestinationChangedListener? {
    val activity = context.run { if (this is AppCompatActivity) this else return null }
    activity.setSupportActionBar(this)
    activity.supportActionBar?.apply {
        setDisplayShowTitleEnabled(true)
        setDisplayShowHomeEnabled(showHome)
        setDisplayHomeAsUpEnabled(homeAsUp)
    }
    setNavigationOnClickListener(navigationOnClickListener)

    val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
        activity.supportActionBar?.title = destination.label
    }
    navController.addOnDestinationChangedListener(listener)
    return null
}