package net.arwix.gastro.client.feature.table.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_open_table_default.view.*
import kotlinx.android.synthetic.main.item_open_table_type.view.*
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.client.R
import net.arwix.gastro.client.feature.table.data.OpenTableItem
import net.arwix.gastro.client.feature.table.data.TableItems
import java.text.NumberFormat

class OpenTableListAdapter(
    private val onChangeCount: (type: String, openTableOrderItem: OpenTableItem, delta: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = mutableListOf<PayAdapterOrderItem>()
    var isClickable = true

    private val doPlusCountClick = View.OnClickListener { view ->
        if (!isClickable) return@OnClickListener
        val item = view.tag as PayAdapterOrderItem.Item
        val currentMaxCount = item.order.orderItem.count - item.order.checkCount
        if (item.order.payCount >= currentMaxCount) return@OnClickListener
        onChangeCount(item.type, item.order, 1)
    }
    private val doMinusCountClick = View.OnClickListener { view ->
        if (!isClickable) return@OnClickListener
        val item = view.tag as PayAdapterOrderItem.Item
        if (item.order.payCount <= 0) return@OnClickListener
        onChangeCount(item.type, item.order, -1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ID_ITEM_DEFAULT ->
                ListItemHolder(
                    inflater.inflate(R.layout.item_open_table_default, parent, false)
                )

            TYPE_ID_ITEM_TYPE ->
                TypeItemHolder(
                    inflater.inflate(R.layout.item_open_table_type, parent, false)
                )

            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (item is PayAdapterOrderItem.Item) {
            holder as ListItemHolder
            holder.minusItemButton.tag = item
            holder.plusItemButton.tag = item
            holder.minusItemButton.setOnClickListener(doMinusCountClick)
            holder.plusItemButton.setOnClickListener(doPlusCountClick)
//            holder.itemView.tag = item
//            holder.itemView.setOnClickListener(doPlusCountClick)
            holder.bindTo(item)
        }
        if (item is PayAdapterOrderItem.Type) {
            holder as TypeItemHolder
            holder.bindTo(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        if (item is PayAdapterOrderItem.Type) return TYPE_ID_ITEM_TYPE
        return TYPE_ID_ITEM_DEFAULT
    }

    fun setItems(openTableOrderData: TableItems) {
        val newList = mutableListOf<PayAdapterOrderItem>()
        openTableOrderData.forEach { (type, list) ->
            val typeItem =
                PayAdapterOrderItem.Type(
                    type
                )
            newList.add(typeItem)
            newList.addAll(list.map {
                PayAdapterOrderItem.Item(
                    type,
                    it
                )
            })
        }
        val diffCallback =
            ItemDiffCallback(items, newList)
        val diffResult =
            DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }


    private class ListItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.item_pay_default_name_text
        private val price: TextView = view.item_pay_default_price_text
        private val payText: TextView = view.item_pay_default_pay_count_text
        val plusItemButton: View = view.item_pay_default_plus_one_button
        val minusItemButton: View = view.item_pay_default_minus_one_button

//        init {
//            itemView.setBackgroundDrawableCompat(R.drawable.selected_list_item)
//        }

        fun bindTo(item: PayAdapterOrderItem.Item) {
            val formatter = NumberFormat.getCurrencyInstance()
            val currentMaxCount = item.order.orderItem.count - item.order.checkCount
            val count = if (item.order.checkCount > 0) {
                "(${item.order.orderItem.count - item.order.returnCount}) "
            } else ""
            name.text = "$count${currentMaxCount}x ${item.order.orderItem.name}"
            price.text =
                formatter.format(item.order.orderItem.price / 100.0).toString() // + "\u20ac"
            plusItemButton.isEnabled = item.order.payCount < currentMaxCount
            minusItemButton.isEnabled = item.order.payCount > 0
            itemView.isEnabled = item.order.payCount < currentMaxCount
            payText.text = itemView.resources.getQuantityString(
                R.plurals.pay_list_item_count_text,
                item.order.payCount,
                item.order.payCount
            )
            if (item.order.payCount > 0) {
                payText.visible()
            } else {
                payText.gone()
            }

        }
    }

    private class TypeItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.item_pay_type_name_text

        fun bindTo(item: PayAdapterOrderItem.Type) {
            title.text = item.name
        }
    }


    private sealed class PayAdapterOrderItem {
        data class Type(val name: String) : PayAdapterOrderItem()
        data class Item(val type: String, val order: OpenTableItem) :
            PayAdapterOrderItem()
    }

    private class ItemDiffCallback(
        private val oldList: List<PayAdapterOrderItem>,
        private val newList: List<PayAdapterOrderItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            if (oldItem is PayAdapterOrderItem.Type && newItem is PayAdapterOrderItem.Type) {
                return oldItem.name == newItem.name
            }
            if (oldItem is PayAdapterOrderItem.Item && newItem is PayAdapterOrderItem.Item) {
                return oldItem.order.orderItem.name == newItem.order.orderItem.name
            }
            return false
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
//            return true
            if (oldItem is PayAdapterOrderItem.Type && newItem is PayAdapterOrderItem.Type) {
                return oldItem.name == newItem.name
            }
            if (oldItem is PayAdapterOrderItem.Item && newItem is PayAdapterOrderItem.Item) {
                return oldItem.order.orderItem.name == newItem.order.orderItem.name &&
                        oldItem.order.orderItem.count == newItem.order.orderItem.count &&
                        oldItem.order.orderItem.price == newItem.order.orderItem.price &&
                        oldItem.order.payCount == newItem.order.payCount
            }
            return false
        }
    }

    private companion object {
        const val TYPE_ID_ITEM_TYPE = 100
        const val TYPE_ID_ITEM_DEFAULT = 200
    }
}