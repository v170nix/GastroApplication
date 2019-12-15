package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_order_list_add_item.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.data.OrderItem
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OrderListAddItemFragment : Fragment() {

    private val orderViewModel: OrderViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_list_add_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        order_list_add_item_name_layout.editText?.editableText.toString()
        order_list_add_item_submit.setOnClickListener {
            val name = order_list_add_item_name_layout.editText?.editableText?.toString() ?: return@setOnClickListener
            if (name.isBlank()) return@setOnClickListener
            val priceString = order_list_add_item_price_layout.editText?.editableText?.toString() ?: return@setOnClickListener
            val priceDouble = priceString.toDoubleOrNull() ?: return@setOnClickListener
            val item = OrderItem(name, (priceDouble * 100).toLong(), 1)
            //   orderViewModel.nonCancelableIntent(OrderViewModel.Action.AddItem(item))
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigateUp()
        }

    }
}
