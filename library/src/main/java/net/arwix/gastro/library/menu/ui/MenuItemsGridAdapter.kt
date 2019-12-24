package net.arwix.gastro.library.menu.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.item_menu_grid_item.view.*
import kotlinx.android.synthetic.main.item_menu_grid_title.view.*
import net.arwix.gastro.library.R
import net.arwix.gastro.library.common.getTextColor
import net.arwix.gastro.library.menu.data.MenuGridItem
import net.arwix.gastro.library.menu.data.MenuGridItem.Companion.VIEW_TYPE_EMPTY
import net.arwix.gastro.library.menu.data.MenuGridItem.Companion.VIEW_TYPE_ITEM
import net.arwix.gastro.library.menu.data.MenuGridItem.Companion.VIEW_TYPE_TITLE
import net.arwix.gastro.library.menu.data.MenuGroupData
import java.text.NumberFormat

class MenuItemsGridAdapter(
    val onClickItem: (menu: MenuGroupData, item: MenuGroupData.PreMenuItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val items = mutableListOf<MenuGridItem>()
    private val formatter = NumberFormat.getCurrencyInstance()

    private val doOnClickItem: View.OnClickListener = View.OnClickListener { view ->
        val adapterItem = view.tag as MenuGridItem.Item
        onClickItem(adapterItem.menu, adapterItem.value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ITEM ->
                ItemViewHolder(inflater.inflate(R.layout.item_menu_grid_item, parent, false))
            VIEW_TYPE_EMPTY -> EmptyViewHolder(parent.context)
            VIEW_TYPE_TITLE -> TitleViewHolder(
                inflater.inflate(R.layout.item_menu_grid_title, parent, false)
            )
            else -> throw IllegalArgumentException()
        }

    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = items[position] as MenuGridItem.Item
                holder.bindTo(item, formatter)
                holder.card.tag = item
                holder.card.setOnClickListener(doOnClickItem)
            }
            is TitleViewHolder -> {
                holder.bindTo(items[position] as MenuGridItem.Title)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].getViewType()

    fun setItems(menuGroupData: MenuGroupData) {
        val rawList = menuGroupData.items ?: return
        val newList = mutableListOf(MenuGridItem.Title(menuGroupData)) +
                rawList.map {
                    MenuGridItem.Item(menuGroupData, it)
                }
        items.clear()
        items.addAll(newList)
        this.notifyDataSetChanged()
    }

    private class EmptyViewHolder(context: Context) : RecyclerView.ViewHolder(View(context))

    private class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.item_menu_grid_item_name_text
        private val price: TextView = view.item_menu_grid_item_price_text
        val card: MaterialCardView = view.item_menu_grid_item_card

        fun bindTo(item: MenuGridItem.Item, numberFormat: NumberFormat) {
            name.text = item.value.name
            price.text = numberFormat.format(item.value.price / 100.0)
            val color = item.value.color ?: item.menu.metadata.color
            if (color != null) {
                card.setCardBackgroundColor(color)
                val textColor = getTextColor(color)
                name.setTextColor(textColor)
                price.setTextColor(textColor)
            } else {
                card.setCardBackgroundColor(Color.TRANSPARENT)
                name.setTextColor(Color.BLACK)
            }
        }
    }

    private class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.item_menu_grid_title_name_text
        private val card: MaterialCardView = view.item_menu_grid_title_card

        fun bindTo(item: MenuGridItem.Title) {
            name.text = item.value.name
            val color = item.value.metadata.color
//            if (color != null) {
//                card.setCardBackgroundColor(color)
//                name.setTextColor(getTextColor(color))
//            } else {
            card.setBackgroundColor(Color.TRANSPARENT)
            name.setTextColor(Color.BLACK)
//            }
        }
    }


}