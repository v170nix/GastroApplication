package net.arwix.gastro.client.ui.check

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_check_detail_default.view.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.data.OrderItem
import java.text.NumberFormat

class CheckDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<OrderItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DefaultHolder(inflater.inflate(R.layout.item_check_detail_default, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as DefaultHolder
        holder.bindTo(items[position])
    }

    fun setItems(checkItems: Map<String, List<OrderItem>>) {
        val newList = mutableListOf<OrderItem>()
        checkItems.forEach {
            newList.addAll(it.value)
        }
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    private class DefaultHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.item_check_detail_default_name_text
        val price: TextView = view.item_check_detail_default_price_text

        fun bindTo(orderItem: OrderItem) {
            val formatter = NumberFormat.getCurrencyInstance()
            name.text = "${orderItem.count}x ${orderItem.name}"
            price.text =
                formatter.format(orderItem.price / 100.0).toString() // + "\u20ac"
        }

    }
}