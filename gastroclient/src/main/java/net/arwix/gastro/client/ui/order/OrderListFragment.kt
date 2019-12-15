package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_order_list.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.data.OrderItem
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OrderListFragment : Fragment() {

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private lateinit var adapterOrder: OrderListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderViewModel.liveState.observe(this, ::render)
//        order_list_add_button.setOnClickListener {
//            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
//                R.id.orderListAddItemFragment
//            )
//        }
        adapterOrder = OrderListAdapter(onTypeClick = {
            Log.e("type click", it)
        })
        with(order_list_recycler_view) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@OrderListFragment.adapterOrder
        }
        order_list_submit_button.setOnClickListener {
            putToDb()
        }
    }

    private fun putToDb() {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        orderViewModel.nonCancelableIntent(OrderViewModel.Action.SubmitOrder(db))
    }

    private fun render(state: OrderViewModel.State) {
        if (state.isSubmit) {
            orderViewModel.clear()
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .popBackStack(R.id.mainClientFragment, true)
        } else {
            adapterOrder.setItems(state.orderItems)
        }
    }

    private sealed class AdapterOrderItems {
        data class Type(val name: String) : AdapterOrderItems()
        data class Default(val type: Type, val order: OrderItem) : AdapterOrderItems()
    }

}
