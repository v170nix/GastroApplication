package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.observe
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_order_list.*
import kotlinx.android.synthetic.main.fragment_order_list.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.client.R
import net.arwix.gastro.client.common.MultilineButtonHelper
import net.arwix.gastro.client.domain.InnerFragmentStateViewModel
import net.arwix.gastro.client.domain.InnerFragmentStateViewModel.InnerFragmentState.OrderListFragmentState
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.library.common.CustomToolbarFragment
import net.arwix.gastro.library.common.asCollapsedFlow
import net.arwix.gastro.library.common.navigate
import net.arwix.gastro.library.common.setToolbarTitle
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGroupData
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.NumberFormat

class OrderListFragment : CustomToolbarFragment(), CoroutineScope by MainScope() {

    override val idResToolbar: Int = R.id.order_list_toolbar

    private val args: OrderListFragmentArgs by navArgs()
    private val fragmentModel: InnerFragmentStateViewModel by sharedViewModel()
    private val orderViewModel: OrderViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private lateinit var stateViewHelper: StateViewHelper
    private lateinit var adapterOrder: OrderListAdapter
    private lateinit var multilineButtonHelper: MultilineButtonHelper
    private lateinit var animationViewHelper: AnimationViewHelper

    private var isSubmit: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateViewHelper = StateViewHelper()
        multilineButtonHelper = MultilineButtonHelper(view.order_list_submit_layout, false)
        animationViewHelper = AnimationViewHelper()
        orderViewModel.liveState.observe(viewLifecycleOwner, ::render)
        adapterOrder = OrderListAdapter(
            onMenuGroupClick = {
                OrderListFragmentDirections.actionToOrderAddPreItemsFragment(
                    it
                ).navigate(this)

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

    override fun onStart() {
        super.onStart()
        stateViewHelper.attach(args.clearInnerState)
    }

    override fun onStop() {
        super.onStop()
        stateViewHelper.detach()
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
        //lock
        isSubmit = if (!isSubmit) state.isSubmit else true
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
        } else if (!isSubmit) {
            animationViewHelper.enableActions()
            adapterOrder.setItems(state.orderItems)
            stateViewHelper.updateAdapterPositions()
            state.tableGroup?.run(::setTitle)
            renderTotalPrice(state.orderItems)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private fun setTitle(tableGroup: TableGroup) {
        setToolbarTitle(getString(R.string.title_order_add))
        collapsing_toolbar_subtitle_text.text =
            "Table ${tableGroup.tableId}/${tableGroup.tablePart}"
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
    }

    private inner class StateViewHelper {

        private var job: Job? = null
        private var isFirstAdapterLoadingItems = true

        fun attach(isClearPreviousState: Boolean = false) {
            isFirstAdapterLoadingItems = true
            if (isClearPreviousState) {
                fragmentModel.setState<OrderListFragmentState>(
                    this@OrderListFragment::class.java
                ) {
                    OrderListFragmentState(true, null)
                }
            }
            fragmentModel.getState<OrderListFragmentState>(this@OrderListFragment::class.java).run {
                order_list_collapsing_app_bar_layout.setExpanded(isExpandTitle, false)
            }
            job?.cancel()
            job = launch {
                order_list_collapsing_app_bar_layout.asCollapsedFlow(null).collect { isCollapsed ->
                    fragmentModel.setState<OrderListFragmentState>(
                        this@OrderListFragment::class.java
                    ) {
                        it.copy(isExpandTitle = !isCollapsed)
                    }
                }
            }
        }

        fun updateAdapterPositions() {
            if (isFirstAdapterLoadingItems) {
                isFirstAdapterLoadingItems = false
                val state =
                    fragmentModel.getState<OrderListFragmentState>(this@OrderListFragment::class.java)
                order_list_recycler_view.layoutManager?.onRestoreInstanceState(state.listState)
            }
        }

        fun detach() {
            job?.cancel()
            isFirstAdapterLoadingItems = false
            val recyclerViewState = order_list_recycler_view.layoutManager?.onSaveInstanceState()
            val state =
                fragmentModel.getState<OrderListFragmentState>(this@OrderListFragment::class.java)
            fragmentModel.setState<OrderListFragmentState>(this@OrderListFragment::class.java) {
                state.copy(listState = recyclerViewState)
            }

        }

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
            delay(300L)
            callback()
            delay(500L)
            order_list_add_process_bar.visible()
        }

    }

}
