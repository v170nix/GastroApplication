package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_order_list.*
import kotlinx.android.synthetic.main.fragment_order_list.view.*
import kotlinx.coroutines.*
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.client.R
import net.arwix.gastro.client.common.MultilineButtonHelper
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGroupData
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.NumberFormat

class OrderListFragment : Fragment(), CoroutineScope by MainScope() {

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private lateinit var adapterOrder: OrderListAdapter
    private lateinit var multilineButtonHelper: MultilineButtonHelper
    private lateinit var animationViewHelper: AnimationViewHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        multilineButtonHelper = MultilineButtonHelper(view.order_list_submit_layout, false)
        animationViewHelper = AnimationViewHelper()
        orderViewModel.liveState.observe(viewLifecycleOwner, ::render)
//        order_list_add_button.setOnClickListener {
//            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
//                R.id.orderListAddItemFragment
//            )
//        }
        adapterOrder = OrderListAdapter(
            onMenuGroupClick = {
                findNavController().navigate(
                    OrderListFragmentDirections.actionToOrderAddPreItemsFragment(
                        it
                    )
                )
            },
            onChangeCount = { type, orderItem, delta ->
                orderViewModel.nonCancelableIntent(
                    OrderViewModel.Action.ChangeCountItem(type, orderItem, delta)
                )
            }

        )
        with(order_list_recycler_view) {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            itemAnimator = null
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
            adapter = this@OrderListFragment.adapterOrder
        }
        multilineButtonHelper.setOnClickListener(View.OnClickListener {
            putToDb()
        })
    }

    private fun putToDb() {
        launch {
            animationViewHelper.toSubmitAction {
                orderViewModel.nonCancelableIntent(
                    OrderViewModel.Action.SubmitOrder(
                        profileViewModel.liveState.value!!.userId!!
                    )
                )
            }
        }
    }

    private fun render(state: OrderViewModel.State) {
        animationViewHelper.enableActions()
        if (state.isSubmit) {
            if (state.resultPrint != null) {
                Toast.makeText(requireContext(), "code: ${state.resultPrint}", Toast.LENGTH_LONG)
                    .apply {
                        this.setGravity(Gravity.CENTER, 0, 0)
                    }.show()
            }
            orderViewModel.clear()
            val options = NavOptions.Builder()
                .setPopUpTo(R.id.openTablesFragment, true)
                .build()
            findNavController(this).navigate(R.id.openTablesFragment, null, options)
        } else {
            adapterOrder.setItems(state.orderItems)
            state.tableGroup?.run(::setTitle)
            renderTotalPrice(state.orderItems)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private fun setTitle(tableGroup: TableGroup) {
        (requireActivity() as AppCompatActivity).supportActionBar?.run {
            title = getString(R.string.title_order_add)
            subtitle = "Table ${tableGroup.tableId}/${tableGroup.tablePart}"
        }
    }

    private fun renderTotalPrice(map: Map<MenuGroupData, List<OrderItem>>) {
        val price =
            map.values.sumBy { it.sumBy { orderItem -> orderItem.price.toInt() * orderItem.count } }
        val counts = map.values.sumBy { it.sumBy { orderItem -> orderItem.count } }
        val formatter = NumberFormat.getCurrencyInstance()

        multilineButtonHelper.setTexts(
            "Items", "Total price",
            counts.toString(), formatter.format(price / 100.0)
        )
//
//        order_list_total_price_text.text =
//            "Total price: ${formatter.format(price / 100.0)}\n($counts items)"
    }

    private inner class AnimationViewHelper {

        fun enableActions() {
            multilineButtonHelper.isEnabled = true
            adapterOrder.isClickable = true
            multilineButtonHelper.visible()
            order_list_add_process_bar.gone()
        }

        suspend fun toSubmitAction(callback: () -> Unit) {
            multilineButtonHelper.isEnabled = false
            adapterOrder.isClickable = false
            multilineButtonHelper.hide()
            callback()
            delay(500L)
            order_list_add_process_bar.visible()
        }

    }

}
