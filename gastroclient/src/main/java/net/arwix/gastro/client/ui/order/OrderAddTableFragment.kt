package net.arwix.gastro.client.ui.order

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_order_add_table.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.common.CustomToolbarFragment
import net.arwix.gastro.library.common.hideKeyboard
import net.arwix.gastro.library.common.navigate
import net.arwix.gastro.library.common.showSoftKeyboard
import net.arwix.gastro.library.data.TableGroup
import org.koin.android.viewmodel.ext.android.sharedViewModel


class OrderAddTableFragment : CustomToolbarFragment() {

    override val idResToolbar: Int = R.id.order_set_table_toolbar
    private val orderViewModel: OrderViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_add_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSoftKeyboard()
        order_table_custom_button.setOnClickListener {
            submitTable()
        }
        order_table_custom_part_input_layout.editText?.setOnEditorActionListener { _, actionId, event ->
            if ((event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) submitTable()
            false
        }
    }

    private fun submitTable() {
        val tableId = runCatching {
            order_table_custom_edit_text.editableText.toString().trim().toInt()
        }.getOrNull() ?: return
        val tablePart = kotlin.runCatching {
            order_table_custom_part_input_layout.editText!!.editableText.toString().trim()
                .toInt()
        }.getOrElse { 1 }
        requireActivity().hideKeyboard()
        toOrderList(TableGroup(tableId, tablePart))
    }

    private fun toOrderList(tableGroup: TableGroup) {
        orderViewModel.selectTable(tableGroup)


        OrderAddTableFragmentDirections
            .actionGlobalOrderListFragment(true)
            .navigate(this)
    }

}
