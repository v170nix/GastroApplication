package net.arwix.gastro.client.domain

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import net.arwix.gastro.client.domain.InnerFragmentStateViewModel.InnerFragmentState.OrderListFragmentState
import net.arwix.gastro.client.ui.order.OrderListFragment

class InnerFragmentStateViewModel : ViewModel() {

    @PublishedApi
    internal val map = mutableMapOf<Class<out Fragment>, InnerFragmentState>(
        OrderListFragment::class.java to OrderListFragmentState(true, null)
    )

    sealed class InnerFragmentState {
        data class OrderListFragmentState(val isExpandTitle: Boolean, val listState: Parcelable?) :
            InnerFragmentState()
    }

    inline fun <reified T : InnerFragmentState> setState(
        clazz: Class<out Fragment>,
        reduce: (state: T) -> T
    ) {
        val s = map[clazz]
        require(s is T)
        map[clazz] = reduce(s)
    }

    inline fun <reified T : InnerFragmentState> getState(clazz: Class<out Fragment>): T {
        val s = map[clazz]
        require(s is T)
        return s
    }


}