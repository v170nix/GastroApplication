package net.arwix.gastro.client.feature.print.ui

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.epson.epos2.Epos2Exception
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import net.arwix.gastro.client.R
import net.arwix.gastro.client.domain.PrinterOrderUseCase
import net.arwix.gastro.library.await
import net.arwix.gastro.library.common.notificationCompatBuilderChannel
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.menu.data.MenuGroupDoc
import net.arwix.gastro.library.order.data.OrderData
import net.arwix.gastro.library.order.domain.OrderPrintUtils
import org.koin.android.ext.android.inject


class PrintIntentService : IntentService("PrintIntentService") {

    private val binder = PrintBinder()
    private val firestoreDbApp: FirestoreDbApp by inject()
    private val broadcastChannel = ConflatedBroadcastChannel<PrintResult>()

    fun getResultAsFlow() = broadcastChannel.asFlow()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_PRINT_ORDER -> {
                val orderRef = intent.getStringExtra(EXTRA_PRINT_ORDER_PARAMS) ?: return
                val result = runBlocking {
                    handlePrintOrder(orderRef)
                }


                val notification = when (result) {
                    is PrintResult.Success -> {
                        notificationCompatBuilderChannel(
                            this,
                            NOTIFICATION_SUCCESS_ORDER_CHANNEL_ID,
                            "Success orders channel", NotificationManagerCompat.IMPORTANCE_LOW
                        ).setSmallIcon(R.drawable.ic_print)
                            .setContentTitle("Print order complete")
                            .setContentText("order id $orderRef")
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .build()
                    }
                    is PrintResult.Error -> {
                        notificationCompatBuilderChannel(
                            this,
                            NOTIFICATION_ERROR_ORDER_CHANNEL_ID,
                            "Error orders channel", NotificationManagerCompat.IMPORTANCE_HIGH
                        ).setSmallIcon(R.drawable.ic_print)
                            .setContentTitle("Print order Error")
                            .setColor(this.resources.getColor(R.color.design_default_color_error))
                            .setContentText("status ${result.printList}")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .build()
                    }
                }
                NotificationManagerCompat.from(this)
                    .notify(12233, notification)

//
//                Handler(Looper.getMainLooper()).post {
//                    Toast.makeText(this.applicationContext, result.toString(), Toast.LENGTH_LONG)
//                        .show()
//                }
            }
        }
    }

    private suspend fun handlePrintOrder(orderRef: String): PrintResult = supervisorScope {
        val orderData = firestoreDbApp.refs.orders.document(orderRef)
            .get().await()?.toObject(OrderData::class.java)!!
        val menuGroups = firestoreDbApp.refs.menu
            .get().await()!!.map { it.toObject(MenuGroupDoc::class.java).toMenuData(it.id) }

        val printerOrderUseCase = PrinterOrderUseCase(applicationContext)
        val awaitPrinterResults = mutableListOf<Deferred<Int>>()
        val prePrinterData = OrderPrintUtils.splitOrderData(menuGroups, orderData)
        prePrinterData.forEach { (printer, orderData) ->
            printer ?: return@forEach
            val job = async(Dispatchers.IO) {
                runCatching {
                    printerOrderUseCase.printOrder(printer, orderData)
                }.getOrElse {
                    if (it is Epos2Exception) it.errorStatus else -100
                }
            }
            awaitPrinterResults.add(job)
        }
        val results = awaitPrinterResults.awaitAll()
        if (results.sumBy { it } == 0) {
            PrintResult.Success(orderData)
        } else {
            PrintResult.Error(orderData, results)
        }
    }

    inner class PrintBinder : Binder() {

        fun getService() = this@PrintIntentService

    }

    companion object {
        @JvmStatic
        fun startPrintOrder(context: Context, orderRef: String) {
            val intent = Intent(context, PrintIntentService::class.java).apply {
                action = ACTION_PRINT_ORDER
                putExtra(EXTRA_PRINT_ORDER_PARAMS, orderRef)
            }
            context.startService(intent)
        }
    }

    sealed class PrintResult {
        data class Success(val orderData: OrderData) : PrintResult()
        data class Error(val orderData: OrderData, val printList: List<Int>) : PrintResult()
    }
}

private const val NOTIFICATION_SUCCESS_ORDER_CHANNEL_ID =
    "PrintService.NotificationOrderSuccessChannel"
private const val NOTIFICATION_ERROR_ORDER_CHANNEL_ID = "PrintService.NotificationOrderErrorChannel"

private const val ACTION_PRINT_ORDER = "net.arwix.gastro.client.print.action.PRINT_ORDER"
private const val EXTRA_PRINT_ORDER_PARAMS =
    "net.arwix.gastro.client.print.action.extra.ORDER_REF"
