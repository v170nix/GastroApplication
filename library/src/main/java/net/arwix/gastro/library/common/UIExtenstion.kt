package net.arwix.gastro.library.common

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs


fun notificationCompatBuilderChannel(
    context: Context,
    channelId: String,
    channelName: String,
    importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT
): NotificationCompat.Builder {
    val channel = context.createNotificationChannel(channelId, channelName, importance)
    return if (channel == null) NotificationCompat.Builder(context) else NotificationCompat.Builder(
        context,
        channelId
    )
}

@SuppressLint("WrongConstant")
fun Context.createNotificationChannel(
    channelId: String,
    channelName: String,
    @SuppressLint("InlinedApi") importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT
): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val chan = NotificationChannel(
            channelId,
            channelName, importance
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
    return null
}

fun NavDirections.navigate(fragment: Fragment) {
    findNavController(fragment).navigate(this)
}

fun Fragment.setToolbarTitle(title: CharSequence?) {
    (requireActivity() as AppCompatActivity).supportActionBar?.apply {
        this.title = title
    }
}

fun AppBarLayout.asCollapsedFlow(init: Boolean?): Flow<Boolean> =
    callbackFlow {
        if (init != null) offer(init!!)
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
            this@asCollapsedFlow.removeOnOffsetChangedListener(listener)
        }
    }.distinctUntilChanged()