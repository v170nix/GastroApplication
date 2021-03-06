package net.arwix.gastro.client.feature.order.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
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
import net.arwix.gastro.library.common.CustomToolbarFragment
import net.arwix.gastro.library.common.navigate
import net.arwix.gastro.library.common.setToolbarTitle
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.order.data.OrderItem
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.NumberFormat

class OrderListFragment : CustomToolbarFragment(), CoroutineScope by MainScope() {

    override val idResToolbar: Int = R.id.order_list_toolbar

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private lateinit var adapterOrder: OrderListAdapter
    private lateinit var multilineButtonHelper: MultilineButtonHelper
    private lateinit var animationViewHelper: AnimationViewHelper
    private var adapterJob: Job? = null
    private var isFirstAfterResume = true

    private var isSubmit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderViewModel.liveState.observe(this, ::render)
        adapterOrder = OrderListAdapter(
            onMenuGroupClick = {
                OrderListFragmentDirections.actionToOrderAddPreItemsFragment(
                    it
                ).navigate(this)

            },
            onChangeCount = { type, orderItem, delta ->
                orderViewModel.nonCancelableIntent(
                    OrderViewModel.Action.ChangeCountItem(
                        type,
                        orderItem,
                        delta
                    )
                )
            }

        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        sharedElementEnterTransition = ChangeBounds().apply {
////            duration = 7500
//        }
//        sharedElementReturnTransition = ChangeBounds().apply {
////            duration = 7500
//        }
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

//    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
//        Log.e("onCreateAnimation", "1")
//        return if (enter) {
//            AnimationUtils.loadAnimation(requireContext(), R.anim.enter_anim)
//        } else super.onCreateAnimation(transit, enter, nextAnim)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        order_list_add_process_bar.gone()
        multilineButtonHelper = MultilineButtonHelper(view.order_list_submit_layout, false)
        animationViewHelper = AnimationViewHelper()
        with(order_list_recycler_view) {
            val linearLayoutManager = LinearLayoutManager(context)
            this.setHasFixedSize(true)
            (itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
            itemAnimator?.moveDuration = 150
            itemAnimator?.changeDuration = 150
            itemAnimator?.addDuration = 80
            itemAnimator?.removeDuration = 80
            layoutManager = linearLayoutManager
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
            adapter = this@OrderListFragment.adapterOrder
        }
        multilineButtonHelper.setOnClickListener(View.OnClickListener {
            putToDb()
        })
    }

    override fun onResume() {
        super.onResume()
        isFirstAfterResume = true
        orderViewModel.liveState.value?.tableGroup?.run(::setTitle)
    }

    override fun onPause() {
        super.onPause()
        isFirstAfterResume = true
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
            orderViewModel.clear()
//            val options = NavOptions.Builder()
//                .setPopUpTo(R.id.openTablesFragment, true)
//                .build()
            findNavController().popBackStack(R.id.openTablesFragment, false)
//            findNavController().navigate(R.id.openTablesFragment, null, options)
        } else if (!isSubmit) {
            if (!state.isLoadingMenu) animationViewHelper.enableActions(orderViewModel.isAnimateBigButton)
            val delayTime = if (isFirstAfterResume) 200L else 0L
            adapterJob?.cancel()
            adapterJob = launch {
                delay(delayTime)
                adapterOrder.setItems(state.orderItems)
                isFirstAfterResume = false
            }
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

    private inner class AnimationViewHelper {

        private var isFirstEnableActions = true
        private var isShowAnimation = false

        fun enableActions(isShowAnimationFirstTime: Boolean) {
            multilineButtonHelper.isEnabled = true
            adapterOrder.isClickable = true
            if (isFirstEnableActions && isShowAnimationFirstTime) {
                if (!isShowAnimation) {
                    isShowAnimation = true
                    launch {
                        delay(100)
                        multilineButtonHelper.show()
                        isShowAnimation = false
                    }
                }
            } else {
                if (!isShowAnimation) multilineButtonHelper.visible()
            }
            isFirstEnableActions = false
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
