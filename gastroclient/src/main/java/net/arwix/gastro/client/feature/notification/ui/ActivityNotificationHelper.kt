package net.arwix.gastro.client.feature.notification.ui

import android.view.View
import kotlinx.android.synthetic.main.activity_main_client.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.client.feature.print.ui.PrintIntentService
import kotlin.coroutines.resume

class ActivityNotificationHelper(
    private val containerView: View,
    private val activityScope: CoroutineScope
) {
    private val printSuccessView: View = containerView.notification_print_success
    private val printErrorView: View = containerView.notification_print_error
    private var isShowContainer: Boolean = false

    init {
        containerView.gone()
        printErrorView.gone()
        printSuccessView.gone()
    }

    suspend fun showNotification(notification: Any) {
        hideChildContainers()
        delay(500L)
        when (notification) {
            is PrintIntentService.PrintResult.Success -> {
                showChildContainer(printSuccessView)
                delay(4000L)
                hideChildContainers()
            }
            is PrintIntentService.PrintResult.Error -> {
                val errorText = notification.printList.joinToString("\n") {
                    "${it.printerAddress}: ${it.message}"
                }
                printErrorView.notification_print_error_info_view.text = errorText
                showChildContainer(printErrorView)

                printErrorView.notification_print_error_dismiss_button.setOnClickListener {
                    activityScope.launch {
                        hideChildContainers()
                    }
                }
            }
        }
    }

    private suspend fun showChildContainer(childView: View) =
        suspendCancellableCoroutine<Boolean> { cont ->
            isShowContainer = true
            containerView.animate().withStartAction {
                containerView.visible()
                childView.visible()
            }.withEndAction {
                cont.resume(true)
            }.translationY(0f).start()
        }

    private suspend fun hideChildContainers() = suspendCancellableCoroutine<Boolean> { cont ->
        if (isShowContainer) {
            containerView.animate()
                .translationY(containerView.height.toFloat())
                .withEndAction {
                    containerView.gone()
                    isShowContainer = false
                    printErrorView.gone()
                    printSuccessView.gone()
                    cont.resume(true)
                }
                .start()
        } else {
            printErrorView.gone()
            printSuccessView.gone()
            cont.resume(false)
        }
    }


}