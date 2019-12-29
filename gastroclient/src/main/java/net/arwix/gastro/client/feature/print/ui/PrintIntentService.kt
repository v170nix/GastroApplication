package net.arwix.gastro.client.feature.print.ui

import android.app.IntentService
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.data.OrderData
import org.koin.android.ext.android.inject


class PrintIntentService : IntentService("PrintIntentService") {

    private val firestoreDbApp: FirestoreDbApp by inject()

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_PRINT_ORDER -> {
                val orderRef = intent.getStringExtra(EXTRA_PRINT_ORDER_PARAMS) ?: return
                runBlocking {
                    handlePrintOrder(orderRef)
                }
            }
        }
    }

    private suspend fun handlePrintOrder(orderRef: String) {
        val orderData =
            firestoreDbApp.refs.orders.document(orderRef).get().await()!!.toObject(OrderData::class.java)!!


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
}

private const val ACTION_PRINT_ORDER = "net.arwix.gastro.client.print.action.PRINT_ORDER"
private const val EXTRA_PRINT_ORDER_PARAMS = "net.arwix.gastro.client.print.action.extra.ORDER_REF"
