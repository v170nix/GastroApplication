package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_order_add_pre_items.*
import net.arwix.extension.toDp
import net.arwix.gastro.client.R
import net.arwix.gastro.library.common.navigate
import net.arwix.gastro.library.menu.MenuUtils
import net.arwix.gastro.library.menu.ui.MenuItemsGridAdapter
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OrderAddPreItemsFragment : Fragment() {

    private val args: OrderAddPreItemsFragmentArgs by navArgs()

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private lateinit var itemsAdapter: MenuItemsGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_order_add_pre_items, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsAdapter = MenuItemsGridAdapter(
            onChangeSelectedItems = { menus ->
                if (menus.isNotEmpty()) {
                    order_add_pre_items_add_selected_button.show()
                    order_add_pre_items_to_custom_item_button.animate()
                        .setDuration(150L)
                        .translationX(-resources.toDp(60f))
                        .start()
                } else {
                    order_add_pre_items_add_selected_button.hide()
                    order_add_pre_items_to_custom_item_button.animate()
                        .setDuration(150L)
                        .translationX(0f)
                        .start()
                }
            }
        )
        with(order_add_pre_items_recycler_view) {
            adapter = itemsAdapter
            layoutManager = MenuUtils.createGridLayoutManager(requireContext(), itemsAdapter)
        }
        orderViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        order_add_pre_items_add_selected_button.setOnClickListener {
            val selectedItems = itemsAdapter.getSelectedItems()
            if (selectedItems.isEmpty()) return@setOnClickListener
            orderViewModel.nonCancelableIntent(
                OrderViewModel.Action.AddItems(selectedItems)
            )
            OrderAddPreItemsFragmentDirections
                .actionGlobalOrderListFragment(false)
                .navigate(this)
        }
        itemsAdapter.setItems(args.menuGroup)
        order_add_pre_items_to_custom_item_button.setOnClickListener {
            OrderAddPreItemsFragmentDirections
                .actionToOrderListAddItemFragment(args.menuGroup)
                .navigate(this)
        }
    }

    private fun render(state: OrderViewModel.State) {
        state.orderItems
        if (state.orderItems.keys.indexOf(args.menuGroup) == -1) {
            // group delete
            itemsAdapter.setItems(listOf())
            OrderAddPreItemsFragmentDirections
                .actionGlobalOrderListFragment()
                .navigate(this)
        }
    }


}
