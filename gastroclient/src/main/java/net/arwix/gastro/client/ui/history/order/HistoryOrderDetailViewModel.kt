package net.arwix.gastro.client.ui.history.order

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import net.arwix.gastro.client.domain.PrinterOrderUseCase
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.MenuData
import net.arwix.gastro.library.data.OrderData
import net.arwix.mvi.SimpleIntentViewModel

class HistoryOrderDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences
) : SimpleIntentViewModel<HistoryOrderDetailViewModel.Action, HistoryOrderDetailViewModel.Result, HistoryOrderDetailViewModel.State>() {

    private lateinit var menuTypes: List<MenuData>

    init {
        viewModelScope.launch {
            val doc = firestore.collection("menu").orderBy("order").get().await()!!
            val menu = doc.documents.map { MenuData(it.id, it.getString("printer")!!) }
            menuTypes = menu
        }
    }


    override fun dispatchAction(action: Action): LiveData<Result> {
        return liveData<Result> {
            val lastCheck =
                firestore.collection("orders").orderBy("created", Query.Direction.DESCENDING)
                    .limit(1).get().await()
            val data = lastCheck!!.documents[0].toObject(OrderData::class.java) ?: return@liveData
            emit(Result.LastOrder(data))
        }
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.LastOrder -> {
                State(orderData = result.orderData)
            }
        }
    }

    suspend fun print(context: Context): Int {
        val orderBonNumber = sharedPreferences.getLong("orderBonNumber", 120555)
        val result = internalViewState.orderData?.run {
            PrinterOrderUseCase(context).printOrder(this, menuTypes, orderBonNumber)
        } ?: orderBonNumber to -100
        sharedPreferences.edit().putLong("orderBonNumber", result.first).apply()
        return result.second
    }


    override var internalViewState: State = State()

    sealed class Action {
        object GetLastOrder : Action()
    }

    sealed class Result {
        data class LastOrder(val orderData: OrderData) : Result()
    }

    data class State(val orderData: OrderData? = null)

}