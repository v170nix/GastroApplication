package net.arwix.gastro.admin.feature.menu.ui


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_admin_menu_item_list.*
import kotlinx.android.synthetic.main.item_admin_menu_item_list.view.*
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.admin.R
import net.arwix.gastro.admin.data.AddEditMode
import net.arwix.gastro.library.common.SimpleRecyclerAdapter
import net.arwix.gastro.library.common.createView
import net.arwix.gastro.library.common.getTextColor
import net.arwix.gastro.library.menu.MenuUtils
import net.arwix.gastro.library.menu.data.ColValue
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.RowValue
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.NumberFormat

class AdminMenuItemListFragment : Fragment() {

    private val itemViewModel: AdminMenuItemViewModel by sharedViewModel()
    //    private val args: AdminMenuItemListFragmentArgs by navArgs()
    private lateinit var adapter: MenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_menu_item_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        adapter = MenuAdapter(
            cellIntersectionColor = ContextCompat.getColor(
                requireContext(),
                R.color.colorCellIntersection
            ),
            cellOutRangeColor = ContextCompat.getColor(
                requireContext(),
                R.color.design_default_color_error
            ),
            onEditGroup = {
                val menuGroupData = itemViewModel.liveState.value?.menu ?: return@MenuAdapter
                findNavController().navigate(
                    AdminMenuItemListFragmentDirections.actionToAdminMenuItemEditFragment(
                        AddEditMode.Edit,
                        menuGroupData,
                        it
                    )
                )
            },
            onDeleteGroup = { menu ->
                MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
                )
                    .setMessage("Delete ${menu.name}?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->
                        itemViewModel.delete(menu)
                    }
                    .show()
            }
        )

        admin_menu_item_recycler_view.apply {
            adapter = this@AdminMenuItemListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        admin_menu_item_add_button.setOnClickListener {
            val menuGroupData = itemViewModel.liveState.value?.menu ?: return@setOnClickListener
            findNavController().navigate(
                AdminMenuItemListFragmentDirections.actionToAdminMenuItemEditFragment(
                    AddEditMode.Add,
                    menuGroupData
                )
            )
        }
    }

    private fun render(state: AdminMenuItemViewModel.State) {
        state.menu?.run menu@{
            setTitle(this)
            items?.run {
                adapter.setItems(this@menu, this)
            } ?: adapter.setItems(listOf())
        }
    }

    private fun setTitle(menuGroupData: MenuGroupData) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            "Menu / ${menuGroupData.name}"
    }

    private class MenuAdapter(
        private val onDeleteGroup: (menuItemData: MenuGroupData.PreMenuItem) -> Unit,
        private val onEditGroup: (menuItemData: MenuGroupData.PreMenuItem) -> Unit,
        @ColorInt private val cellIntersectionColor: Int,
        @ColorInt private val cellOutRangeColor: Int
    ) : SimpleRecyclerAdapter<MenuGroupData.PreMenuItem>(

        onCreate = { inflater, parent, _ ->
            MenuHolder(inflater.createView(R.layout.item_admin_menu_item_list, parent))
        },
        diffUtilFactory = { oldList, newList ->
            ItemDiffCallback(oldList, newList)
        }
    ) {
        private var menuGroup: MenuGroupData? = null
        private var cellsCount = mutableMapOf<Pair<RowValue, ColValue>, Int>()


        private val doDeleteClick = View.OnClickListener {
            val item = it.tag as MenuGroupData.PreMenuItem
            onDeleteGroup(item)
        }

        private val doEditClick = View.OnClickListener {
            val item = it.tag as MenuGroupData.PreMenuItem
            onEditGroup(item)
        }

        fun setItems(menuGroupData: MenuGroupData, list: List<MenuGroupData.PreMenuItem>) {
            menuGroup = menuGroupData
//            if (this.items == list) return
            cellsCount.clear()
            list.forEach {
                val cell = it.row to it.col
                val count = cellsCount.getOrPut(cell) { 0 }
                cellsCount[cell] = count + 1
            }
            super.setItems(list)
            cellsCount.clear()
        }

        override fun onBindViewHolder(holder: Holder<MenuGroupData.PreMenuItem>, position: Int) {
            super.onBindViewHolder(holder, position)
            val item = items[position]
            with(holder as MenuHolder) {
                deleteButton.tag = item
                itemView.tag = item
                deleteButton.setOnClickListener(doDeleteClick)
                itemView.setOnClickListener(doEditClick)

                val count = cellsCount[item.row to item.col] ?: 1

                holder.order.setTextColor(holder.printer.textColors)
                holder.order.setBackgroundColor(Color.TRANSPARENT)
                if (count > 1) {
                    holder.order.setBackgroundColor(cellIntersectionColor)
                    holder.order.setTextColor(getTextColor(cellIntersectionColor))
                }
                if (item.col > MenuUtils.maxTableCols) {
                    holder.order.setBackgroundColor(cellOutRangeColor)
                    holder.order.setTextColor(getTextColor(cellOutRangeColor))
                }

                val color = item.color ?: menuGroup?.metadata?.color
                color?.run {
                    colorView.setBackgroundColor(this)
                } ?: run {
                    colorView.background = null
                }
            }
        }

        private class MenuHolder(view: View) :
            SimpleRecyclerAdapter.Holder<MenuGroupData.PreMenuItem>(view) {
            private val numberFormatter = NumberFormat.getCurrencyInstance()
            val name: TextView = view.item_admin_menu_item_name_text
            val printer: TextView = view.item_admin_menu_item_printer_text
            val order: TextView = view.item_admin_menu_item_order_text
            val price: TextView = view.item_admin_menu_item_price_text
            val deleteButton: View = view.item_admin_menu_item_delete_button
            val colorView: View = view.item_admin_menu_item_color

            override fun bindTo(item: MenuGroupData.PreMenuItem) {
                name.text = item.name
                item.printer?.run {
                    printer.visible()
                    printer.text = this
                } ?: printer.gone()
                order.text = itemView.resources.getString(
                    R.string.admin_menu_item_order,
                    item.row,
                    item.col
                )
                price.text = numberFormatter.format(item.price / 100.0)
            }
        }

        private class ItemDiffCallback(
            private val oldList: List<MenuGroupData.PreMenuItem>,
            private val newList: List<MenuGroupData.PreMenuItem>
        ) : DiffUtil.Callback() {

            private val oldCellCount: Map<Pair<RowValue, ColValue>, Int> = getCellsFromList(oldList)
            private val newCellCount: Map<Pair<RowValue, ColValue>, Int> = getCellsFromList(newList)

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
                return oldItem == newItem && oldCellCount[oldItem.row to oldItem.col] == newCellCount[newItem.row to newItem.row]
            }
        }

        companion object {
            fun getCellsFromList(list: List<MenuGroupData.PreMenuItem>): Map<Pair<RowValue, ColValue>, Int> {
                val result = mutableMapOf<Pair<RowValue, ColValue>, Int>()
                list.forEach {
                    val cell = it.row to it.col
                    val count = result.getOrPut(cell) { 0 }
                    result[cell] = count + 1
                }
                return result
            }
        }
    }

}
