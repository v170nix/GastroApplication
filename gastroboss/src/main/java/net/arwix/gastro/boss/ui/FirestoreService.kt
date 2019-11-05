package net.arwix.gastro.boss.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.boss.MainBossActivity
import net.arwix.gastro.boss.R
import net.arwix.gastro.library.snapshotFlow
import org.json.JSONObject

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
                doPrint()
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

    private var mWebView: WebView? = null

    private fun doPrint() {
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false

            override fun onPageFinished(view: WebView, url: String?) {
                Log.e("onPageFinished", "page finished loading $url")
                super.onPageFinished(view, url)
                createWebPrintJob(view)
                mWebView = null
            }
        }
        val htmlDocument =  "<html><body><h1>Test Content</h1><p>Testing, testing, testing...</p></body></html>"
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
        mWebView = webView

    }

    private fun createWebPrintJob(webView: WebView) {
        (getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->
            val jobName = "Doc"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
           val printJob = printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder().build()
            )
        }
    }

    private fun getPrinters(token: String) {
        val req = Request.Builder()
            .url("https://www.google.com/cloudprint/search")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()
        val client = OkHttpClient()
        launch {
            val response = client.newCall(req).execute()
            val json = JSONObject(response.body().string())
            Log.e("result", json.toString(5))
        }

    }
}
