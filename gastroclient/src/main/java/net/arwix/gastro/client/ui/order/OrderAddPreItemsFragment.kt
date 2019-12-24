package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import kotlinx.android.synthetic.main.fragment_order_add_pre_items.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.menu.MenuUtils
import net.arwix.gastro.library.menu.ui.MenuItemsGridAdapter
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OrderAddPreItemsFragment : Fragment() {

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private lateinit var itemsAdapter: MenuItemsGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_order_add_pre_items, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsAdapter = MenuItemsGridAdapter(
            onClickItem = { menu, item ->
                orderViewModel.nonCancelableIntent(
                    OrderViewModel.Action.AddItem(
                        menu.name,
                        OrderItem(item.name!!, item.price, 1)
                    )
                )
                findNavController(this).navigateUp()
            },
            onAddCustomItem = {
                findNavController(this).navigate(
                    R.id.orderListAddItemFragment,
                    bundleOf(OrderViewModel.BUNDLE_ID_ITEM_TYPE to it)
                )
            }
        )
        with(order_add_pre_items_recycler_view) {
            adapter = itemsAdapter
            layoutManager = MenuUtils.createGridLayoutManager(requireContext(), itemsAdapter)
        }
        orderViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
//        order_add_custom_item_button.setOnClickListener {
//                    findNavController(this).navigate(
//                    R.id.orderListAddItemFragment,
//                    bundleOf(OrderViewModel.BUNDLE_ID_ITEM_TYPE to it)
//                )
//        }
    }

    private fun render(state: OrderViewModel.State) {
        val list = state.orderItems.keys.toSortedSet(compareBy { it.metadata.order }).toList()
        itemsAdapter.setItems(list)
    }


}
