package net.arwix.gastro.admin.feature.menu.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_admin_menu_group_list.*
import kotlinx.android.synthetic.main.item_admin_menu_group_list.view.*
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.admin.R
import net.arwix.gastro.library.common.SimpleRecyclerAdapter
import net.arwix.gastro.library.common.createView
import net.arwix.gastro.library.menu.data.MenuGroupData
import org.koin.android.viewmodel.ext.android.sharedViewModel

class AdminMenuGroupListFragment : Fragment() {

    private lateinit var adapter: MenuAdapter
    private val menuViewModel: AdminMenuViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_menu_group_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        adapter = MenuAdapter(
            onDeleteItem = { menu ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("")
            }
        )
        admin_menu_recycler_view.apply {
            adapter = this@AdminMenuGroupListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }

    private fun render(state: AdminMenuViewModel.State) {
        state.menuGroups?.run {
            adapter.setItems(this)
        }
    }


    private class MenuAdapter(
        private val onDeleteItem: (menuGroupGroupData: MenuGroupData) -> Unit
    ) : SimpleRecyclerAdapter<MenuGroupData>(
        onCreate = { inflater, parent, _ ->
            MenuHolder(inflater.createView(R.layout.item_admin_menu_group_list, parent))
        }
    ) {

        private val doDeleteClick = View.OnClickListener {
            val item = it.tag as MenuHolder

        }

        override fun onBindViewHolder(holder: Holder<MenuGroupData>, position: Int) {
            super.onBindViewHolder(holder, position)
            holder as MenuHolder
            holder.deleteGroupButton.tag = items[position]
            holder.deleteGroupButton.setOnClickListener {

            }
        }

        private class MenuHolder(view: View) : SimpleRecyclerAdapter.Holder<MenuGroupData>(view) {
            val name: TextView = view.item_admin_menu_group_name_text
            val printer: TextView = view.item_admin_menu_group_printer_text
            val order: TextView = view.item_admin_menu_group_order_text
            val items: TextView = view.item_admin_menu_group_items_count_text
            val editGroupButton: Button = view.item_admin_menu_group_edit_button
            val deleteGroupButton: Button = view.item_admin_menu_group_delete_button
            val viewItemsButton: Button = view.item_admin_menu_group_edit_items_button

            override fun bindTo(item: MenuGroupData) {
                name.text = item.name
                item.printer?.run {
                    printer.visible()
                    printer.text = this
                } ?: printer.gone()
                order.text = (item.metadata.order?.toString() ?: "").let { "order: $it" }
                items.text = (item.items?.size ?: 0).let { "items: $it" }
            }
        }
    }

}


