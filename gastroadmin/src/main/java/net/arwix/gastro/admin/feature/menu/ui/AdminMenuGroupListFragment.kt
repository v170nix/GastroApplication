package net.arwix.gastro.admin.feature.menu.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_admin_menu_group_list.*
import kotlinx.android.synthetic.main.item_admin_menu_group_list.view.*
import kotlinx.coroutines.*
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.admin.R
import net.arwix.gastro.admin.data.AddEditMode
import net.arwix.gastro.library.common.SimpleRecyclerAdapter
import net.arwix.gastro.library.common.createView
import net.arwix.gastro.library.menu.data.MenuGroupData
import org.koin.android.viewmodel.ext.android.sharedViewModel

class AdminMenuGroupListFragment : Fragment(), CoroutineScope by MainScope() {

    private val args: AdminMenuGroupListFragmentArgs by navArgs()
    private lateinit var adapter: MenuAdapter
    private val menuGroupViewModel: AdminMenuGroupViewModel by sharedViewModel()
    private var isFirstScrollPositionCompleted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_menu_group_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuGroupViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        adapter = MenuAdapter(
            onEditGroup = {
                findNavController().navigate(
                    AdminMenuGroupListFragmentDirections.actionToMenuGroupEditFragment(
                        it,
                        AddEditMode.Edit
                    )
                )
            },
            onDeleteGroup = { menu ->
                MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
                )
                    .setMessage("Delete menu?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->
                        menuGroupViewModel.nonCancelableIntent(
                            AdminMenuGroupViewModel.Action.DeleteMenu(
                                menu
                            )
                        )
                    }
                    .show()
            },
            onViewItems = {

            }
        )

        admin_menu_group_recycler_view.apply {
            adapter = this@AdminMenuGroupListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        admin_menu_group_add_button.setOnClickListener {
            findNavController().navigate(
                AdminMenuGroupListFragmentDirections.actionToMenuGroupEditFragment(
                    null,
                    AddEditMode.Add
                )
            )
        }

    }

    private fun render(state: AdminMenuGroupViewModel.State) {
        state.menuGroups?.run {
            adapter.setItems(this)
            val submitMenuGroup = args.submitMenuGroup ?: return@run
            val position = adapter.getPosition(submitMenuGroup) ?: return@run
            if (isFirstScrollPositionCompleted) return@run
            launch {
                delay(100)
                admin_menu_group_recycler_view.smoothScrollToPosition(position)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private class MenuAdapter(
        private val onDeleteGroup: (menuGroupGroupData: MenuGroupData) -> Unit,
        private val onEditGroup: (menuGroupGroupData: MenuGroupData) -> Unit,
        private val onViewItems: (menuGroupGroupData: MenuGroupData) -> Unit
    ) : SimpleRecyclerAdapter<MenuGroupData>(
        onCreate = { inflater, parent, _ ->
            MenuHolder(inflater.createView(R.layout.item_admin_menu_group_list, parent))
        },
        diffUtilFactory = { oldList, newList ->
            ItemDiffCallback(oldList, newList)
        }
    ) {

        private val doDeleteClick = View.OnClickListener {
            val item = it.tag as MenuGroupData
            onDeleteGroup(item)
        }

        private val doEditClick = View.OnClickListener {
            val item = it.tag as MenuGroupData
            onEditGroup(item)
        }

        private val doViewItemsClick = View.OnClickListener {
            val item = it.tag as MenuGroupData
            onViewItems(item)
        }

        fun getPosition(data: MenuGroupData): Int? {
            items.forEachIndexed { index, menuGroupData ->
                if (data.name == menuGroupData.name) return index
            }
            return null
        }

        override fun setItems(list: List<MenuGroupData>) {
            if (this.items == list) return
            super.setItems(list)
        }

        override fun onBindViewHolder(holder: Holder<MenuGroupData>, position: Int) {
            super.onBindViewHolder(holder, position)
            val item = items[position]
            holder as MenuHolder
            holder.deleteGroupButton.tag = item
            holder.editGroupButton.tag = item
            holder.itemView.tag = item
            holder.deleteGroupButton.setOnClickListener(doDeleteClick)
            holder.editGroupButton.setOnClickListener(doEditClick)
            holder.itemView.setOnClickListener(doViewItemsClick)
        }

        private class MenuHolder(view: View) : SimpleRecyclerAdapter.Holder<MenuGroupData>(view) {
            val name: TextView = view.item_admin_menu_group_name_text
            val printer: TextView = view.item_admin_menu_group_printer_text
            val order: TextView = view.item_admin_menu_group_order_text
            val items: TextView = view.item_admin_menu_group_items_count_text
            val editGroupButton: Button = view.item_admin_menu_group_edit_button
            val deleteGroupButton: View = view.item_admin_menu_group_delete_button
            val colorView: View = view.item_admin_menu_group_color

            override fun bindTo(item: MenuGroupData) {
                name.text = item.name
                item.printer?.run {
                    printer.visible()
                    printer.text = this
                } ?: printer.gone()

                order.text = item.metadata.order?.run {
                    itemView.resources.getString(
                        R.string.admin_menu_group_item_order, this
                    )
                } ?: ""
                items.text =
                    itemView.resources.getString(
                        R.string.admin_menu_group_item_items, item.items?.size ?: 0
                    )
                item.metadata.color?.run {
                    colorView.setBackgroundColor(this)
                } ?: run {
                    colorView.background = null
                }

            }
        }

        private class ItemDiffCallback(
            private val oldList: List<MenuGroupData>,
            private val newList: List<MenuGroupData>
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
                return oldItem.items?.size == newItem.items?.size &&
                        oldItem.metadata.updatedTime?.toEpochMilli() == newItem.metadata.updatedTime?.toEpochMilli()
            }
        }
    }

}


