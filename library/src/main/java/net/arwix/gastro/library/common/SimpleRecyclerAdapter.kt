package net.arwix.gastro.library.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

open class SimpleRecyclerAdapter<T>(
    private val onCreate: (inflater: LayoutInflater, parent: ViewGroup, viewType: Int) -> Holder<T>,
    private val diffUtilFactory: ((oldList: List<T>, newList: List<T>) -> DiffUtil.Callback)? = null

) : RecyclerView.Adapter<SimpleRecyclerAdapter.Holder<T>>() {

    protected val items = mutableListOf<T>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<T> {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        return onCreate(inflater, parent, viewType)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: Holder<T>, position: Int) {
        holder.bindTo(items[position])
    }

    open fun setItems(list: List<T>) {
        if (diffUtilFactory != null) {
            val diffCallback = diffUtilFactory.invoke(items, list)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            items.clear()
            items.addAll(list)
            diffResult.dispatchUpdatesTo(this)
        } else {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }
    }

    abstract class Holder<M>(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bindTo(item: M)

    }

}

fun LayoutInflater.createView(@LayoutRes layoutRes: Int, parent: ViewGroup): View =
    this.inflate(layoutRes, parent, false)