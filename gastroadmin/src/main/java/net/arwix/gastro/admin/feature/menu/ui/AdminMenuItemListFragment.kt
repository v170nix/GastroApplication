package net.arwix.gastro.admin.feature.menu.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import net.arwix.gastro.library.menu.data.MenuGroupData
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.NumberFormat

class AdminMenuItemListFragment : Fragment() {

    private val itemViewModel: AdminMenuItemViewModel by sharedViewModel()
    private val args: AdminMenuItemListFragmentArgs by navArgs()
    private lateinit var adapter: MenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_menu_item_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        adapter = MenuAdapter(
            onEditGroup = {
                findNavController().navigate(
                    AdminMenuItemListFragmentDirections.actionToAdminMenuItemEditFragment(
                        AddEditMode.Edit,
                        args.MenuGroup,
                        it
                    )
                )
            },
            onDeleteGroup = { menu ->
                MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
                )
                    .setMessage("Delete menu item?")
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
            findNavController().navigate(
                AdminMenuItemListFragmentDirections.actionToAdminMenuItemEditFragment(
                    AddEditMode.Add,
                    args.MenuGroup
                )
            )
        }
    }

    private fun render(state: AdminMenuItemViewModel.State) {
        state.menu?.run {
            setTitle(this)
            items?.run(adapter::setItems) ?: adapter.setItems(listOf())
//            val position = adapter.getPosition(submitMenuGroup) ?: return@run
//            if (isFirstScrollPositionCompleted) return@run
//            launch {
//                delay(100)
//                admin_menu_group_recycler_view.smoothScrollToPosition(position)
//            }
        }
    }

    private fun setTitle(menuGroupData: MenuGroupData) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            "Menu / ${menuGroupData.name}"
    }

    private class MenuAdapter(
        private val onDeleteGroup: (menuItemData: MenuGroupData.PreMenuItem) -> Unit,
        private val onEditGroup: (menuItemData: MenuGroupData.PreMenuItem) -> Unit
    ) : SimpleRecyclerAdapter<MenuGroupData.PreMenuItem>(
        onCreate = { inflater, parent, _ ->
            MenuHolder(inflater.createView(R.layout.item_admin_menu_item_list, parent))
        },
        diffUtilFactory = { oldList, newList ->
            ItemDiffCallback(oldList, newList)
        }
    ) {

        private val doDeleteClick = View.OnClickListener {
            val item = it.tag as MenuGroupData.PreMenuItem
            onDeleteGroup(item)
        }

        private val doEditClick = View.OnClickListener {
            val item = it.tag as MenuGroupData.PreMenuItem
            onEditGroup(item)
        }

        fun getPosition(data: MenuGroupData): Int? {
            items.forEachIndexed { index, menuGroupData ->
                if (data.name == menuGroupData.name) return index
            }
            return null
        }

        override fun setItems(list: List<MenuGroupData.PreMenuItem>) {
            if (this.items == list) return
            super.setItems(list)
        }

        override fun onBindViewHolder(holder: Holder<MenuGroupData.PreMenuItem>, position: Int) {
            super.onBindViewHolder(holder, position)
            val item = items[position]
            holder as MenuHolder
            holder.deleteButton.tag = item
            holder.itemView.tag = item
            holder.deleteButton.setOnClickListener(doDeleteClick)
            holder.itemView.setOnClickListener(doEditClick)
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
                    R.string.admin_menu_group_item_order,
                    item.position
                )
                price.text = numberFormatter.format(item.price / 100.0)

                item.color?.run {
                    colorView.setBackgroundColor(this)
                } ?: run {
                    colorView.background = null
                }

            }
        }

        private class ItemDiffCallback(
            private val oldList: List<MenuGroupData.PreMenuItem>,
            private val newList: List<MenuGroupData.PreMenuItem>
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
                return oldItem == newItem
            }
        }
    }

}
