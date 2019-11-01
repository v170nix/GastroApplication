package net.arwix.gastro.boss.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.boss.MainBossActivity
import net.arwix.gastro.boss.R
import net.arwix.gastro.library.snapshotFlow

class FirestoreService : Service(), CoroutineScope by MainScope() {

    override fun onCreate() {
        super.onCreate()
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("firestore_service", "Firestore Service")
        } else {
            ""
        }
        val notification =  NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_print)
            .setTicker("ticker text")
            .build()
        val notificationIntent = Intent(this, MainBossActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        startForeground(6969, notification)
        launch {
            pending()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") stopSelf()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
        Log.e("Test", "Service: onDestroy")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cancel()
        Log.e("Test", "Service: onTaskRemoved")
    }

    private suspend fun pending() {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val orders: CollectionReference = db.collection("orders")
        orders
            .snapshotFlow()
            .catch { e ->
                Log.e("catch", e.toString())
            }
            .collect {
                Log.e("docs", it.documents.toString())
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}
