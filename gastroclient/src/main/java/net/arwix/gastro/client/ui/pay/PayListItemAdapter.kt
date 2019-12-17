package net.arwix.gastro.client.ui.pay

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_pay_default.view.*
import kotlinx.android.synthetic.main.item_pay_type.view.*
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.client.R
import java.text.NumberFormat

class PayListItemAdapter(
    private val onChangeCount: (type: String, payOrderItem: PayViewModel.PayOrderItem, delta: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = mutableListOf<PayAdapterOrderItem>()

    private val doPlusCountClick = View.OnClickListener { view ->
        val item = view.tag as PayAdapterOrderItem.Item
        if (item.order.payCount >= item.order.orderItem.count) return@OnClickListener
        onChangeCount(item.type, item.order, 1)
    }
    private val doMinusCountClick = View.OnClickListener { view ->
        val item = view.tag as PayAdapterOrderItem.Item
        if (item.order.payCount <= 0) return@OnClickListener
        onChangeCount(item.type, item.order, -1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ID_ITEM_DEFAULT ->
                ListItemHolder(inflater.inflate(R.layout.item_pay_default, parent, false))

            TYPE_ID_ITEM_TYPE ->
                TypeItemHolder(inflater.inflate(R.layout.item_pay_type, parent, false))

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

    fun setItems(payOrderData: MutableMap<String, MutableList<PayViewModel.PayOrderItem>>) {
        Log.e("setItems", payOrderData.toString())
        val newList = mutableListOf<PayAdapterOrderItem>()
        payOrderData.forEach { (type, list) ->
            val typeItem = PayAdapterOrderItem.Type(type)
            newList.add(typeItem)
            newList.addAll(list.map { PayAdapterOrderItem.Item(type, it) })
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


    private class ListItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.item_pay_default_name_text
        private val price: TextView = view.item_pay_default_price_text
        private val payText: TextView = view.item_pay_default_pay_count_text
        val plusItemButton: View = view.item_pay_default_plus_one_button
        val minusItemButton: View = view.item_pay_default_minus_one_button

        fun bindTo(item: PayAdapterOrderItem.Item) {
            val formatter = NumberFormat.getCurrencyInstance()
            name.text = "${item.order.orderItem.count}x ${item.order.orderItem.name}"
            price.text =
                formatter.format(item.order.orderItem.price / 100.0).toString() // + "\u20ac"
            plusItemButton.isEnabled = item.order.payCount < item.order.orderItem.count
            minusItemButton.isEnabled = item.order.payCount > 0
            payText.text = "${item.order.payCount} items(s)"
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
        data class Item(val type: String, val order: PayViewModel.PayOrderItem) :
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
            return (oldItem == newItem)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
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