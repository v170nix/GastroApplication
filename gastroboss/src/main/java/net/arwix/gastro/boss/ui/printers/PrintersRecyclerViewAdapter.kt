package net.arwix.gastro.boss.ui.printers


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_printer.view.*
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.data.printer.Printer
import net.arwix.gastro.boss.data.printer.Printers

class PrintersRecyclerViewAdapter : RecyclerView.Adapter<PrintersRecyclerViewAdapter.ViewHolder>() {
    private var printersData: Printers? = null
    private var selectedData: MutableList<Printer> = mutableListOf()

    private val clickListener: View.OnClickListener

    init {
        clickListener = View.OnClickListener { v ->
            val item = v.tag as Printer
            val oldSelectedItem = selectedData.find { it.id == item.id }
            if (oldSelectedItem != null) {
                selectedData.remove(oldSelectedItem)
            } else {
                selectedData.add(item)
            }
            val ids = printersData?.printers?.indexOf(item) ?: return@OnClickListener
            notifyItemChanged(ids)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_printer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = printersData!!.printers!![position]
        holder.setData(item)
        with(holder.itemView) {
            tag = item
            setOnClickListener(clickListener)
            this.isActivated = selectedData.find { it.id == item.id } != null
        }
    }

    fun getSelectedItems(): List<Printer> = selectedData

    override fun getItemCount(): Int = printersData?.printers?.size ?: 0

    fun setData(printers: Printers, selectedPrinters: List<Printer>) {
        printersData = printers
        selectedData = selectedPrinters.toMutableList()
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.printer_item_display_name
        private val descriptionText: TextView = view.printer_item_description
        private val statusText: TextView = view.printer_item_status

        init {
            view.setBackgroundResource(R.drawable.selected_list_item)
        }

        fun setData(printer: Printer) {
            nameText.text = printer.displayName
            descriptionText.text = printer.description
            statusText.text = printer.connectionStatus
        }
    }
}
