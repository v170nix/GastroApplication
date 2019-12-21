package net.arwix.gastro.client.ui.table

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_open_tables.view.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.TableGroup

class OpenTablesAdapter(
    onItemClick: (tableGroup: TableGroup) -> Unit,
    onAddOrderClick: (tableGroup: TableGroup) -> Unit
) : RecyclerView.Adapter<OpenTablesAdapter.ItemsHolder>() {

    private val items = mutableListOf<OpenTableAdapterItem>()
    private val doItemClick = View.OnClickListener {
        val item = it.tag as OpenTableAdapterItem
        onItemClick(item.tableGroup)
    }
    private val doAddOrderClick = View.OnClickListener {
        val item = it.tag as OpenTableAdapterItem
        onAddOrderClick(item.tableGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemsHolder(
            inflater.inflate(
                R.layout.item_open_tables,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ItemsHolder, position: Int) {
        holder.bindTo(items[position])
        holder.itemView.tag = items[position]
        holder.itemView.setOnClickListener(doItemClick)
        holder.addOrderButton.tag = items[position]
        holder.addOrderButton.setOnClickListener(doAddOrderClick)
    }

    fun setData(data: Map<TableGroup, OpenTableData>) {
        val newList = mutableListOf<OpenTableAdapterItem>()
        data.forEach { (tableGroup, data) ->
            newList.add(OpenTableAdapterItem(tableGroup, data))
        }
        val diffCallback = ItemDiffCallback(items, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class ItemsHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val table: TextView = view.item_open_tables_table_id_text
        val addOrderButton: Button = view.item_open_tables_add_order_button

        init {
            view.setBackgroundResource(R.drawable.selected_list_item)
        }

        fun bindTo(item: OpenTableAdapterItem) {
            table.text =
                "${item.tableGroup.tableId}/${item.tableGroup.tablePart} - count(${item.tableData.orders?.size})"
        }
    }

    data class OpenTableAdapterItem(
        val tableGroup: TableGroup,
        val tableData: OpenTableData
    )

    private class ItemDiffCallback(
        private val oldList: List<OpenTableAdapterItem>,
        private val newList: List<OpenTableAdapterItem>
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
            return oldItem.tableGroup == newItem.tableGroup && oldItem.tableData.updated == newItem.tableData.updated
        }
    }

}