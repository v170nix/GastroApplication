package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_order_add_item.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.common.hideKeyboard
import net.arwix.gastro.library.common.showSoftKeyboard
import net.arwix.gastro.library.data.OrderItem
import org.koin.android.viewmodel.ext.android.sharedViewModel
import kotlin.math.roundToLong

class OrderAddItemFragment : Fragment() {

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
        (requireActivity() as AppCompatActivity).supportActionBar?.title = args.menuGroup.name
        order_list_add_item_name_layout.editText?.editableText.toString()
        order_list_add_item_submit.setOnClickListener {
            val name = order_list_add_item_name_layout.editText?.editableText?.toString()
                ?: return@setOnClickListener
            if (name.isBlank()) return@setOnClickListener
            val priceString = order_list_add_item_price_layout.editText?.editableText?.toString()
                ?: return@setOnClickListener
            val priceDouble = priceString.toDoubleOrNull() ?: return@setOnClickListener
            if (priceDouble <= 0) return@setOnClickListener
            val item = OrderItem(name, (priceDouble * 100).roundToLong(), 1)
            orderViewModel.nonCancelableIntent(
                OrderViewModel.Action.AddItem(
                    args.menuGroup.name,
                    item
                )
            )
            hideKeyboard()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.orderListFragment, true)
                .build()
            findNavController(this).navigate(R.id.orderListFragment, null, navOptions)
        }
        order_list_add_item_back_button.setOnClickListener {
            requireActivity().hideKeyboard()
            findNavController(this).popBackStack()
        }


    }
}
