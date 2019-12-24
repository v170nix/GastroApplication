package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_order_add_pre_items.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.menu.MenuUtils
import net.arwix.gastro.library.menu.ui.MenuItemsGridAdapter

class OrderAddPreItemsFragment : Fragment() {

    private lateinit var itemsAdapter: MenuItemsGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_order_add_pre_items, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsAdapter = MenuItemsGridAdapter(
            onClickItem = { menu, item ->
                Log.e("item", item.toString())

            }
        )
        with(order_add_pre_items_recycler_view) {
            adapter = itemsAdapter
            layoutManager = MenuUtils.createGridLayoutManager(requireContext(), itemsAdapter)
        }
    }


}
