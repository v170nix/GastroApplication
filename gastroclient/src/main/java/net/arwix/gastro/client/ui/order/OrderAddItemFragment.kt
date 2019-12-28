package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_order_add_item.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.common.CustomToolbarFragment
import net.arwix.gastro.library.common.hideKeyboard
import net.arwix.gastro.library.common.setToolbarTitle
import net.arwix.gastro.library.common.showSoftKeyboard
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGroupData
import org.koin.android.viewmodel.ext.android.sharedViewModel
import kotlin.math.roundToLong

class OrderAddItemFragment : CustomToolbarFragment() {

    override val idResToolbar: Int = R.id.order_add_items_toolbar

    private val args: OrderAddItemFragmentArgs by navArgs()
    private val orderViewModel: OrderViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_add_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSoftKeyboard()
        orderViewModel.liveState.value?.run {
            setTitle(tableGroup!!, args.menuGroup)
        }
        order_list_add_item_price_layout.editText?.setOnEditorActionListener { v, actionId, event ->
            if ((event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) addItem()
            false
        }
        order_list_add_item_submit.setOnClickListener { addItem() }
        order_list_add_item_back_button.setOnClickListener {
            hideKeyboard()
            findNavController(this).popBackStack()
        }
    }

    private fun setTitle(tableGroup: TableGroup, menuGroup: MenuGroupData) {
        setToolbarTitle(menuGroup.name)
        collapsing_toolbar_subtitle_text.text =
            "Table ${tableGroup.tableId}/${tableGroup.tablePart}"
    }

    private fun addItem() {
        val name = order_list_add_item_name_layout.editText?.editableText?.toString() ?: return
        if (name.isBlank()) return
        val priceString =
            order_list_add_item_price_layout.editText?.editableText?.toString() ?: return
        val priceDouble = priceString.toDoubleOrNull() ?: return
        if (priceDouble <= 0) return
        val item = OrderItem(name, (priceDouble * 100).roundToLong(), 1)
        orderViewModel.nonCancelableIntent(
            OrderViewModel.Action.AddItem(
                args.menuGroup.name,
                item
            )
        )
        hideKeyboard()
        findNavController().popBackStack(R.id.orderListFragment, false)
    }
}
