package net.arwix.gastro.client.ui.pay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.pager_pay_list.view.*
import net.arwix.gastro.client.R

class PayListAdapter(
    private val onChangePayCount: (part: PayViewModel.StatePart, type: String, payOrderItem: PayViewModel.PayOrderItem, delta: Int) -> Unit
) : RecyclerView.Adapter<PayListAdapter.PagerHolder>() {

    private var items = listOf<PayViewModel.StatePart>()

    private val doChangeCount: onPayChangeViewListener =
        { view, type, payOrderItem, delta ->
            val item = view.tag as PayViewModel.StatePart
            onChangePayCount(item, type, payOrderItem, delta)
        }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PagerHolder, position: Int) {
        holder.bindTo(items[position])
        holder.itemView.tag = items[position]
        holder.setChangePayListener(doChangeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.pager_pay_list, parent, false)
        return PagerHolder(view)
    }


    fun setPartList(list: List<PayViewModel.StatePart>) {
        items = list
        notifyDataSetChanged()
    }


    class PagerHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val recyclerView: RecyclerView = view.pager_pay_order_recycler_view
        private var listener: onPayChangeViewListener? = null
        private val itemAdapter = PayListItemAdapter { type, payOrderItem, delta ->
            listener?.invoke(itemView, type, payOrderItem, delta)
        }

        init {
            val linearLayoutManager = LinearLayoutManager(itemView.context)
            with(recyclerView) {
                layoutManager = linearLayoutManager
                itemAnimator = null
                addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
                adapter = itemAdapter
            }
        }

        fun setChangePayListener(listener: onPayChangeViewListener) {
            this.listener = listener
        }

        fun bindTo(item: PayViewModel.StatePart) {
            itemAdapter.setItems(item.payOrderData)
        }
    }
}

typealias onPayChangeViewListener = (view: View, type: String, payOrderItem: PayViewModel.PayOrderItem, delta: Int) -> Unit