package net.arwix.gastro.boss.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.print.PdfPrint
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.boss.MainBossActivity
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.domain.PrintJobUseCase
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.toFlow
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class FirestoreService : Service(), CoroutineScope by MainScope() {

    private val printJobUseCase: PrintJobUseCase by inject()
    private var counter = 0

    override fun onCreate() {
        super.onCreate()
        printJobUseCase.attachScope(this)
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("firestore_service", "Firestore Service")
        } else {
            ""
        }
        val notification = NotificationCompat.Builder(this, channelId)
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
//        cancel()
        Log.e("Test", "Service: onTaskRemoved")
    }

    private suspend fun pending() {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val orders = db.collection("orders")
        val query = orders
            .whereEqualTo("isPrinted", false)
            .orderBy("timestampCreated", Query.Direction.DESCENDING)

        query
            .toFlow()
            .catch { e ->
                Log.e("catch", e.toString())
            }
            .collect {
                Log.e("docs", it.documents.toString())
                it.documents.lastOrNull()?.let { doc ->
                    val orderData = doc.toObject(OrderData::class.java)!!
                    Log.e("for each", orderData.toString())
                    orders.document(doc.id).update("isPrinted", true).await()
                    Log.e("for each", "isPrinted = true")
                    //        orderData.isPrinted = true

                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun doPrint(document: DocumentSnapshot) {
        val orderData = document.toObject(OrderData::class.java)
        Log.e("order data", orderData.toString())
        orderData ?: return
//        val pdf = PdfDocument()
//        val a4Width = (210 / 25.4 * 72).toInt()
//        val a4Height = (297 / 25.4 * 72).toInt()
//        val page = pdf.startPage(PdfDocument.PageInfo.Builder(a4Width, a4Height, 1).create())
//        val canvas = page.canvas
//        canvas.drawText("раз два три", a4Width / 2f, a4Height / 2f, Paint().apply {
//            textSize = a4Width / 10f
//            color = Color.BLACK
//        })
//        pdf.finishPage(page)
        launch {
            //            printJobUseCase.print(pdf)
            val adapter = createDoc(document.id, orderData)
            val path = createFile(adapter)
            printJobUseCase.print(path, document.id)
        }
    }

    private suspend fun createFile(adapter: PrintDocumentAdapter) =
        suspendCoroutine<String> { cont ->
            PdfPrint(
                PrintAttributes
                    .Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_C8)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 203, 203))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            ).print(adapter,
                this@FirestoreService.cacheDir, "test.pdf", object : PdfPrint.CallbackPrint {
                    override fun success(path: String) {
                        Log.e("success", "create file $path")
                        cont.resume(path)
                    }

                    override fun onFailure(errorMsg: String) {
                        Log.e("error", "create file")
                        cont.resumeWithException(Error(errorMsg))
                    }
                }
            )
        }

    private suspend fun createDoc(documentName: String, orderData: OrderData) =
        suspendCoroutine<PrintDocumentAdapter> { cont ->
            val webView = WebView(this)
            webView.clearCache(true)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ) =
                    false

                override fun onPageFinished(view: WebView, url: String?) {
                    val printAdapter = view.createPrintDocumentAdapter(documentName)
                    cont.resume(printAdapter)
                }
            }
//            val items = orderData.orderItems!!.joinToString("") {
//                "<tr><td width=\"100%\" style=\"font-size: 10px\">${it.count}x ${it.name}</td><td style=\"font-size: 10px\">${it.price / 100.0}</td></tr>"
//            }

//            val cal = Calendar.getInstance().apply {
//                this.timeInMillis = orderData.timestampCreated!!.seconds * 1000L
//            }
            val dateFormat = SimpleDateFormat("MM.dd hh:mm:ss", Locale.getDefault())

            val htmlDocument =
                "<html><head><style>" +
                        "h3 {font-size: 12px;};" +
                        "</style></head>" +
                        "<body><h3>table №${orderData.table}</br>${dateFormat.format(orderData.created!!.toDate())}</h3>" +
                        "<table width=\"100%\">" +
//                        "$items</table>" +
                        "<p style=\"font-size:5px\" align=\"right\">" +
                        "order - $documentName</p>" +
                        "</body></html>"
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
        }

    private suspend fun toDoc(printAdapter: PrintDocumentAdapter) =
        suspendCoroutine<String> { cont ->
            val printAttributes = PrintAttributes.Builder().build()
            PdfPrint(printAttributes).print(
                printAdapter,
                this.cacheDir, "test.pdf", object : PdfPrint.CallbackPrint {
                    override fun success(path: String) {
                        cont.resume(path)
                    }

                    override fun onFailure(errorMsg: String) {
                        cont.resumeWithException(Error(errorMsg))
                    }

                }
            )

        }


    private fun createWebPrintJob(webView: WebView) {
//        (getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->
//            val jobName = "Doc"
//            val printAdapter = webView.createPrintDocumentAdapter(jobName)
//           val printJob = printManager.print(
//                jobName,
//                printAdapter,
//                PrintAttributes.Builder().build()
//            )
//        }
    }

}
