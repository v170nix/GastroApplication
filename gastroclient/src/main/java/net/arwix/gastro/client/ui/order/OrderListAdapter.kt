package net.arwix.gastro.client.ui.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_order_list.view.*
import kotlinx.android.synthetic.main.item_type_list.view.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.data.OrderItem
import java.text.NumberFormat

class OrderListAdapter(
    val onTypeClick: (type: String) -> Unit,
    val onChangeCount: (type: String, orderItem: OrderItem, delta: Int) -> Unit
) : RecyclerView.Adapter<OrderListAdapter.AdapterItemHolder>() {

    private val items = mutableListOf<AdapterOrderItems>()
    private val doTypeClick = View.OnClickListener { view ->
        val type = view.tag as AdapterOrderItems.Type
        onTypeClick(type.name)
    }
    private val doPlusCountClick = View.OnClickListener { view ->
        val orderItem = view.tag as AdapterOrderItems.Default
        onChangeCount(orderItem.type.name, orderItem.order, 1)
    }
    private val doMinusCountClick = View.OnClickListener { view ->
        val orderItem = view.tag as AdapterOrderItems.Default
        if (orderItem.order.count == 0) return@OnClickListener
        onChangeCount(orderItem.type.name, orderItem.order, -1)
    }

    fun setItems(newMap: MutableMap<String, List<OrderItem>>) {
        val newList = mutableListOf<AdapterOrderItems>()
        newMap.forEach { (type, list) ->
            val typeItem =
                AdapterOrderItems.Type(
                    type
                )
            newList.add(typeItem)
            newList.addAll(list.map {
                AdapterOrderItems.Default(
                    typeItem,
                    it
                )
            })
        }
        val diffCallback =
            ItemDiffCallback(
                items,
                newList
            )
        val diffResult =
            DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ID_ITEM_ORDER -> {
                AdapterItemHolder.ListItemHolder(
                    inflater.inflate(
                        R.layout.item_order_list,
                        parent,
                        false
                    )
                )
            }
            TYPE_ID_ITEM_TYPE -> {
                AdapterItemHolder.TypeItemHolder(
                    inflater.inflate(
                        R.layout.item_type_list,
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int = items.count()

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        if (item is AdapterOrderItems.Type) return TYPE_ID_ITEM_TYPE
        return TYPE_ID_ITEM_ORDER
    }

    override fun onBindViewHolder(holder: AdapterItemHolder, position: Int) {
        val item = items[position]
        if (item is AdapterOrderItems.Default) {
            holder as AdapterItemHolder.ListItemHolder
            holder.minusItemButton.tag = item
            holder.plusItemButton.tag = item
            holder.minusItemButton.setOnClickListener(doMinusCountClick)
            holder.plusItemButton.setOnClickListener(doPlusCountClick)
            holder.minusItemButton.isEnabled = item.order.count != 0
            holder.bindTo(item)
        }
        if (item is AdapterOrderItems.Type) {
            holder as AdapterItemHolder.TypeItemHolder
            holder.addItemButton.tag = item
            holder.addItemButton.setOnClickListener(doTypeClick)
            holder.bindTo(item)
        }
    }

    sealed class AdapterOrderItems {
        data class Type(val name: String) : AdapterOrderItems()
        data class Default(val type: Type, val order: OrderItem) : AdapterOrderItems()
    }

    private class ItemDiffCallback(
        private val oldList: List<AdapterOrderItems>,
        private val newList: List<AdapterOrderItems>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return (oldItem == newItem)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            if (oldItem is AdapterOrderItems.Type && newItem is AdapterOrderItems.Type) {
                return oldItem.name == newItem.name
            }
            if (oldItem is AdapterOrderItems.Default && newItem is AdapterOrderItems.Default) {
                return oldItem.order.name == newItem.order.name &&
                        oldItem.order.count == newItem.order.count &&
                        oldItem.order.price == newItem.order.price
            }
            return false
        }

    }

    sealed class AdapterItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        class ListItemHolder(view: View) : AdapterItemHolder(view) {
            private val name: TextView = view.item_order_list_add_name_text
            private val price: TextView = view.item_order_list_add_price_text
            val plusItemButton: View = view.item_order_list_add_plus_one_button
            val minusItemButton: View = view.item_order_list_add_minus_one_button

            fun bindTo(item: AdapterOrderItems.Default) {
                val formatter = NumberFormat.getCurrencyInstance()
                name.text = "${item.order.count}x ${item.order.name}"
                price.text = formatter.format(item.order.price / 100.0).toString() // + "\u20ac"
            }
        }

        class TypeItemHolder(view: View) : AdapterItemHolder(view) {
            private val title: TextView = view.item_type_list_add_name_text
            val addItemButton: View = view.item_type_list_add_plus_one_button

            fun bindTo(item: AdapterOrderItems.Type) {
                title.text = item.name
            }
        }
    }

    private companion object {
        const val TYPE_ID_ITEM_TYPE = 10
        const val TYPE_ID_ITEM_ORDER = 20
    }


}