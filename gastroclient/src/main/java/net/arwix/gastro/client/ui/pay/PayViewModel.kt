package net.arwix.gastro.client.ui.pay

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.toFlow
import net.arwix.mvi.SimpleIntentViewModel

class PayViewModel(
    private val firestore: FirebaseFirestore
) : SimpleIntentViewModel<PayViewModel.Action, PayViewModel.Result, PayViewModel.State>() {

    private var tableUpdateJob: Job? = null
    override var internalViewState: State = State()

    fun setTable(tableId: Int) {
        tableUpdateJob?.cancel()
        notificationFromObserver(Result.InitTable(tableId))
        tableUpdateJob = viewModelScope.launch {
            firestore.collection("open tables").document(tableId.toString()).toFlow()
                .collect {
                    it.toObject(OpenTableData::class.java)?.run {
                        val parts = (this.parts ?: return@run).mapNotNull { doc ->
                            val orderData = doc.get().await()?.toObject(OrderData::class.java)
                                ?: return@mapNotNull null
                            val orderItems = orderData.orderItems ?: return@mapNotNull null
                            val payOrderItems =
                                orderItems.mapValues { (_, listOrderItems) ->
                                    listOrderItems.map { orderItem ->
                                        PayOrderItem(orderItem = orderItem)
                                    }.toMutableList()
                                }
                            StatePart(
                                doc,
                                PayOrderData(
                                    orderData, payOrderItems
                                )
                            )
                        }
                        notificationFromObserver(Result.TableData(tableId, parts))
                    }
                }
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> {
        return liveData<Result> {
            when (action) {
                is Action.ChangePayCount -> {
                    emit(
                        Result.ChangePayCount(
                            action.statePart,
                            action.type,
                            action.payOrderItem,
                            action.delta
                        )
                    )
                }
            }
        }
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.TableData -> {
                internalViewState.copy(
                    tableId = result.tableId,
                    parts = result.data
                )
            }
            is Result.InitTable -> {
                State(tableId = result.tableId)
            }
            is Result.ChangePayCount -> {
                val list = result.statePart.payOrderData.payOrderItems.getValue(result.type)
                val index = list.indexOf(result.payOrderItem)
                list[index] = result.payOrderItem.copy(
                    payCount = result.payOrderItem.payCount + result.delta
                )
                internalViewState
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    sealed class Action {
        data class ChangePayCount(
            val statePart: StatePart,
            val type: String,
            val payOrderItem: PayOrderItem,
            val delta: Int
        ) : Action()
    }

    sealed class Result {
        data class InitTable(val tableId: Int) : Result()
        data class TableData(val tableId: Int, val data: List<StatePart>) : Result()
        data class ChangePayCount(
            val statePart: StatePart,
            val type: String,
            val payOrderItem: PayOrderItem,
            val delta: Int
        ) : Result()
    }

    data class State(
        val isLoadingParts: Boolean = true,
        val tableId: Int? = null,
        val parts: List<StatePart>? = null
    )

    data class StatePart(val reference: DocumentReference, val payOrderData: PayOrderData)
    data class PayOrderData(
        val orderData: OrderData,
        val payOrderItems: Map<String, MutableList<PayOrderItem>>
    )

    data class PayOrderItem(val payCount: Int = 0, val orderItem: OrderItem)

}