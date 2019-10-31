package net.arwix.gastro.client.ui.order


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_order_list.*
import kotlinx.android.synthetic.main.item_order_list.view.*
import net.arwix.gastro.client.R
import net.arwix.gastro.client.data.OrderItem
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OrderListFragment : Fragment() {

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private lateinit var adapter: ListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderViewModel.liveState.observe(this, ::render)
        order_list_add_button.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
                R.id.orderListAddItemFragment
            )
        }
        adapter = ListAdapter()
        with(order_list_recycler_view) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@OrderListFragment.adapter
        }
    }

    private fun render(state: OrderViewModel.State) {
        adapter.setItems(state.orderItems)
    }

    private class ListAdapter: RecyclerView.Adapter<ListAdapter.ListItemHolder>() {
        private val items = mutableListOf<OrderItem>()

        fun setItems(newList: List<OrderItem>) {
            Log.e("list", newList.toString())
            val diffCallback = ItemDiffCallback(items, newList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            items.clear()
            items.addAll(newList)
            diffResult.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_order_list, parent, false)
            return ListItemHolder(view)
        }

        override fun getItemCount(): Int = items.count()

        override fun onBindViewHolder(holder: ListItemHolder, position: Int) {
            val item = items[position]
            holder.name.text = item.name
            holder.price.text = (item.price / 100.0).toString()
        }

        private class ListItemHolder(view: View): RecyclerView.ViewHolder(view) {
            val name: TextView = view.item_order_list_add_name_text
            val price: TextView = view.item_order_list_add_price_text
        }
    }

    private class ItemDiffCallback(
        private val oldList: List<OrderItem>,
        private val newList: List<OrderItem>
    ): DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }

    }
}
