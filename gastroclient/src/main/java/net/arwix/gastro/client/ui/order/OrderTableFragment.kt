package net.arwix.gastro.client.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_order_table.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.common.hideKeyboard
import net.arwix.gastro.library.common.showSoftKeyboard
import net.arwix.gastro.library.data.TableGroup
import org.koin.android.viewmodel.ext.android.sharedViewModel


class OrderTableFragment : Fragment() {

    private val orderViewModel: OrderViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderViewModel.liveState.observe(this, ::render)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSoftKeyboard()
        order_table_custom_button.setOnClickListener {
            val tableId = runCatching {
                order_table_custom_edit_text.editableText.toString().trim().toInt()
            }.getOrNull() ?: return@setOnClickListener
            val tablePart = kotlin.runCatching {
                order_table_custom_part_input_layout.editText!!.editableText.toString().trim()
                    .toInt()
            }.getOrElse { 1 }
            requireActivity().hideKeyboard()
            toOrderList(TableGroup(tableId, tablePart))
        }
    }

    private fun render(state: OrderViewModel.State) {
    }

    private fun toOrderList(tableGroup: TableGroup) {
        orderViewModel.selectTable(tableGroup)
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
            R.id.orderListFragment
        )
    }

}
