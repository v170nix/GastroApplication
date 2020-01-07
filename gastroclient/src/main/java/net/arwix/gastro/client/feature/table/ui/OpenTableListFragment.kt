package net.arwix.gastro.client.feature.table.ui


import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_open_table_list.*
import kotlinx.coroutines.*
import net.arwix.extension.invisible
import net.arwix.extension.visible
import net.arwix.gastro.client.R
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.library.common.CustomToolbarFragment
import net.arwix.gastro.library.common.setToolbarTitle
import net.arwix.gastro.library.data.TableGroup
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.NumberFormat

class OpenTableListFragment : CustomToolbarFragment(), CoroutineScope by MainScope() {

    override val idResToolbar: Int = R.id.open_table_list_toolbar

    private val openTableViewModel: OpenTableViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private lateinit var adapter: OpenTableListAdapter
    private lateinit var fabAnimationHelper: FabAnimationHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_open_table_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabAnimationHelper = FabAnimationHelper()
        adapter =
            OpenTableListAdapter { type, payOrderItem, delta ->
                openTableViewModel.nonCancelableIntent(
                    OpenTableViewModel.Action.ChangePayCount(
                        type,
                        payOrderItem,
                        delta
                    )
                )
                //  listener?.invoke(itemView, type, payOrderItem, delta)
            }
        val linearLayoutManager = LinearLayoutManager(requireContext())
        with(pay_list_order_recycler_view) {
            itemAnimator?.changeDuration = 60
            layoutManager = linearLayoutManager
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    context,
                    linearLayoutManager.orientation
                )
            )
            adapter = this@OpenTableListFragment.adapter
        }

        pay_list_add_all_to_pay_button.setOnClickListener {
            openTableViewModel.nonCancelableIntent(OpenTableViewModel.Action.AddAllItemsToPay)
        }

        pay_list_submit_button.setOnClickListener {
            val userId = profileViewModel.liveState.value?.userId ?: return@setOnClickListener
            setIsEnableButtons(false)
            openTableViewModel.nonCancelableIntent(OpenTableViewModel.Action.Checkout(userId))
        }

        pay_list_delete_button.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete selected items?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm") { _, _ ->
                    setIsEnableButtons(false)
                    val userId =
                        profileViewModel.liveState.value?.userId ?: return@setPositiveButton
                    openTableViewModel.nonCancelableIntent(
                        OpenTableViewModel.Action.DeleteCheckout(
                            userId
                        )
                    )
                }
                .show()
        }
        open_table_list_split_button.setOnClickListener {
            findNavController().navigate(R.id.splitSelectTableFragment)
        }

        openTableViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancel()
    }

    private fun render(state: OpenTableViewModel.State) {
        setIsEnableButtons(true)
        updateTextAndVisiblePayButton(openTableViewModel.liveState.value)
        state.tableGroup?.run(this::setTitle)
        state.tableItems?.run {
            adapter.setItems(this)
        }
        if (state.isCloseTableGroup) {
            Toast.makeText(
                requireContext(),
                "close table ${state.tableGroup?.toPrintString()}",
                Toast.LENGTH_LONG
            )
                .apply {
                    this.setGravity(Gravity.CENTER, 0, 0)
                }
                .show()
            findNavController().navigateUp()
        }
    }

    private fun setIsEnableButtons(isEnabled: Boolean) {
        adapter.isClickable = isEnabled
        launch {
            delay(250L)
            pay_list_delete_button.isEnabled = isEnabled
            open_table_list_split_button.isEnabled = isEnabled
            pay_list_submit_button.isEnabled = isEnabled
//            pay_list_delete_button.backgroundTintList = if (isEnabled)
//                ContextCompat.getColorStateList(
//                    requireContext(),
//                    R.color.design_default_color_error
//                )
//            else
//                ContextCompat.getColorStateList(
//                    requireContext(),
//                    R.color.mtrl_btn_bg_color_selector
//                )
        }
    }


    private fun setTitle(tableGroup: TableGroup) {
        setToolbarTitle(resources.getString(R.string.pay_list_title))
        collapsing_toolbar_subtitle_text.text =
            "Table ${tableGroup.tableId}/${tableGroup.tablePart}"
//        (requireActivity() as AppCompatActivity).supportActionBar?.title =
//            "Table ${tableGroup.tableId}/${tableGroup.tablePart} "
    }

    private fun getPayCountAndPrice(state: OpenTableViewModel.State?): Pair<Long, Int>? {
        state ?: return null
        val payOrderItems = state.tableItems ?: return null
        var count = 0
        var price = 0L
        payOrderItems.values.forEach { payOrderItem ->
            payOrderItem.forEach {
                if (it.payCount > 0) {
                    count += it.payCount
                    price += it.orderItem.price * it.payCount
                }
            }
        }
        return price to count
    }

    private fun updateTextAndVisiblePayButton(state: OpenTableViewModel.State?) {
        getPayCountAndPrice(state)?.let { (price, count) ->
            if (price > 0) {
                pay_list_submit_button.show()
                fabAnimationHelper.show()
            } else {
                pay_list_submit_button.hide()
                fabAnimationHelper.hide()
                return
            }
            val formatter = NumberFormat.getCurrencyInstance()
            pay_list_submit_button.text = "To pay: ${formatter.format(price / 100.0)}"
        } ?: run {
            pay_list_submit_button.hide()
            fabAnimationHelper.hide()
        }
    }

    private inner class FabAnimationHelper {

        private var isOpen = false

        init {
            open_table_list_menu_button.setOnClickListener {
                if (isOpen) showOut() else showIn()
                isOpen = !isOpen
            }
        }

        fun show() {
            open_table_list_menu_button.show()
            if (isOpen) showOut()
            isOpen = false
        }

        fun hide() {
            open_table_list_menu_button.hide()
            if (isOpen) showOut()
            isOpen = false
        }

        fun showIn() {
            rotateFab(open_table_list_menu_button, true)
            showIn(open_table_list_split_button).start()
            showIn(pay_list_delete_button).setDuration(300).start()
        }

        fun showOut() {
            rotateFab(open_table_list_menu_button, false)
            showOut(open_table_list_split_button).setDuration(300).start()
            showOut(pay_list_delete_button).start()
        }
    }

    private companion object {
        fun rotateFab(view: View, rotate: Boolean) {
            view.animate()
                .setDuration(200L)
                .withEndAction {}
                .rotation(if (rotate) 135f else 0f)
                .start()
        }

        fun showIn(view: View): ViewPropertyAnimator {
            view.visible()
            view.alpha = 0f
            view.translationY = view.height.toFloat()
            return view.animate().setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .translationY(0f)
                .alpha(1f)
        }

        fun showOut(view: View): ViewPropertyAnimator {
            view.visible()
            view.alpha = 1f
            view.translationY = 0f
            return view.animate().setDuration(200)
                .translationY(view.height.toFloat())
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    view.invisible()
                }
                .alpha(0f)
        }
    }

}
