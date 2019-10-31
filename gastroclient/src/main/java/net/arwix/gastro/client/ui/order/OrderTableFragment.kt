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
        order_table_custom_button.setOnClickListener {
            val tableNumber = runCatching {
                order_table_custom_edit_text.editableText.toString().toInt()
            }.getOrNull() ?: return@setOnClickListener
            toOrderList(tableNumber)
        }
        order_table_1_button.setOnClickListener {
            toOrderList(1)
        }
        order_table_2_button.setOnClickListener {
            toOrderList(2)
        }
        order_table_3_button.setOnClickListener {
            toOrderList(3)
        }
    }

    private fun render(state: OrderViewModel.State) {
    }

    private fun toOrderList(tableNumber: Int) {
        orderViewModel.selectTable(tableNumber)
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
            R.id.orderListFragment
        )
    }

}
