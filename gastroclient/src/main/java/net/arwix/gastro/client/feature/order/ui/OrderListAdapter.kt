package net.arwix.gastro.client.feature.order.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_order_menu_group.view.*
import kotlinx.android.synthetic.main.item_order_menu_item.view.*
import net.arwix.extension.setBackgroundDrawableCompat
import net.arwix.gastro.client.R
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.order.data.OrderItem
import java.text.NumberFormat

class OrderListAdapter(
    val onMenuGroupClick: (type: MenuGroupData) -> Unit,
    val onChangeCount: (type: MenuGroupData, orderItem: OrderItem, delta: Int) -> Unit
) : RecyclerView.Adapter<OrderListAdapter.AdapterItemHolder>() {

    var isClickable = true
        set(value) {
            if (field != value) {
                items.forEachIndexed { index, item ->
                    if (item is AdapterOrderItems.Default) {
                        if (item.order.count > 0) {
                            notifyItemChanged(index)
                        }
                    }
                }
                field = value
            }
        }

    private var items = mutableListOf<AdapterOrderItems>()

    private val doTypeClick = View.OnClickListener { view ->
        if (!isClickable) return@OnClickListener
        val type = view.tag as AdapterOrderItems.Type
        onMenuGroupClick(type.groupData)
    }
    private val doPlusCountClick = View.OnClickListener { view ->
        if (!isClickable) return@OnClickListener
        val orderItem = view.tag as AdapterOrderItems.Default
        onChangeCount(orderItem.type.groupData, orderItem.order, 1)
    }
    private val doMinusCountClick = View.OnClickListener { view ->
        if (!isClickable) return@OnClickListener
        val orderItem = view.tag as AdapterOrderItems.Default
        if (orderItem.order.count <= 0) return@OnClickListener
        onChangeCount(orderItem.type.groupData, orderItem.order, -1)
    }

    fun setItems(newMap: Map<MenuGroupData, List<OrderItem>>) {
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
                items.toMutableList(),
                newList
            )
        val diffResult =
            DiffUtil.calculateDiff(diffCallback)
//        items = newList
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
                        R.layout.item_order_menu_item,
                        parent,
                        false
                    )
                )
            }
            TYPE_ID_ITEM_TYPE -> {
                AdapterItemHolder.TypeItemHolder(
                    inflater.inflate(
                        R.layout.item_order_menu_group,
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
            holder.minusItemButton.isEnabled = (item.order.count > 0) && isClickable
            holder.plusItemButton.isEnabled = isClickable
            holder.itemView.isEnabled = isClickable
            holder.bindTo(item)
//            holder.itemView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    holder.animateItem()
//                    holder.itemView.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                }
//
//            })
        }
        if (item is AdapterOrderItems.Type) {
            holder as AdapterItemHolder.TypeItemHolder
            holder.addItemButton.tag = item
            holder.addItemButton.setOnClickListener(doTypeClick)
            holder.itemView.tag = item
            holder.itemView.setOnClickListener(doTypeClick)
            holder.itemView.setBackgroundDrawableCompat(R.drawable.selected_list_group)
            holder.bindTo(item)
        }
    }


    sealed class AdapterOrderItems {
        data class Type(val groupData: MenuGroupData) : AdapterOrderItems()
        data class Default(val type: Type, val order: OrderItem) : AdapterOrderItems()
    }

    private class ItemDiffCallback(
        private val oldList: List<AdapterOrderItems>,
        private val newList: List<AdapterOrderItems>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return false
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            if (oldItem is AdapterOrderItems.Type && newItem is AdapterOrderItems.Type) {
                return oldItem.groupData.name == newItem.groupData.name
            }
            if (oldItem is AdapterOrderItems.Default && newItem is AdapterOrderItems.Default) {
                return oldItem.order.name == newItem.order.name
            }
            return false
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return false
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            if (oldItem is AdapterOrderItems.Type && newItem is AdapterOrderItems.Type) {
                return oldItem.groupData.name == newItem.groupData.name
            }
            if (oldItem is AdapterOrderItems.Default && newItem is AdapterOrderItems.Default) {
                return (oldItem.order.name == newItem.order.name &&
                        oldItem.order.count == newItem.order.count &&
                        oldItem.order.price == newItem.order.price)
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

//            init {
//                itemView.setBackgroundDrawableCompat(R.drawable.selected_list_item)
//            }

            fun bindTo(item: AdapterOrderItems.Default) {
                val formatter = NumberFormat.getCurrencyInstance()
                name.text = "${item.order.count}x ${item.order.name}"
                price.text = formatter.format(item.order.price / 100.0).toString() // + "\u20ac"
            }

            fun animateItem() {
                val itemWidth: Float = itemView.width.toFloat()
                val animator = TranslateAnimation(-itemWidth, 0f, 0f, 0f)

                animator.repeatCount = 0
                animator.interpolator = AccelerateInterpolator(1.0f)
                animator.duration = 700
                animator.fillAfter = true

                itemView.animation = animator
                itemView.startAnimation(animator)
            }
        }

        class TypeItemHolder(view: View) : AdapterItemHolder(view) {
            val title: TextView = view.item_type_list_add_name_text
            val addItemButton: View = view.item_type_list_add_plus_one_button

            fun bindTo(item: AdapterOrderItems.Type) {
                title.text = item.groupData.name
            }
        }
    }

    private companion object {
        const val TYPE_ID_ITEM_TYPE = 10
        const val TYPE_ID_ITEM_ORDER = 20
    }


}