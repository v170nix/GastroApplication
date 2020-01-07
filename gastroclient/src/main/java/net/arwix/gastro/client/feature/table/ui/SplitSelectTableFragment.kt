package net.arwix.gastro.client.feature.table.ui


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_split_select_table.*
import net.arwix.gastro.client.R
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.client.ui.table.OpenTablesAdapter
import net.arwix.gastro.client.ui.table.OpenTablesViewModel
import net.arwix.gastro.library.common.CustomToolbarFragment
import net.arwix.gastro.library.common.hideKeyboard
import net.arwix.gastro.library.common.setToolbarTitle
import net.arwix.gastro.library.data.TableGroup
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SplitSelectTableFragment : CustomToolbarFragment() {

    override val idResToolbar: Int = R.id.split_select_table_toolbar

    private val openTablesViewModel: OpenTablesViewModel by sharedViewModel()
    private val openTableViewModel: OpenTableViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private lateinit var adapter: OpenTablesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_split_select_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openTablesViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        adapter = OpenTablesAdapter(
            isShowAddButton = false,
            onItemClick = {
                split_select_custom_input_layout.editText?.setText(it.tableId.toString())
                split_select_custom_part_input_layout.editText?.setText(it.tablePart.toString())
            },
            onAddOrderClick = {}
        )
        with(split_select_open_tables_recycler_view) {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
            adapter = this@SplitSelectTableFragment.adapter
        }
        openTableViewModel.liveState.value?.tableGroup?.run(::setTitle)
        split_select_table_submit_button.setOnClickListener {
            submitTable()
        }
    }

    private fun submitTable() {
        val tableId = runCatching {
            val l =
                split_select_custom_input_layout.editText!!.editableText.toString().trim().toInt()
            Log.e("l", l.toString())
            l
        }.getOrNull() ?: return
        val tablePart = runCatching {
            split_select_custom_part_input_layout.editText!!.editableText.toString().trim()
                .toInt()
        }.getOrNull() ?: return
        val currentTable = openTableViewModel.liveState.value?.tableGroup ?: return
        if (tableId == currentTable.tableId && tablePart == currentTable.tablePart) return
        val userId = profileViewModel.liveState.value?.userId ?: return
        openTableViewModel.split(userId, TableGroup(tableId, tablePart))
        hideKeyboard()
        findNavController().popBackStack()
//        toOrderList(TableGroup(tableId, tablePart))
    }

    private fun render(state: OpenTablesViewModel.State) {
        state.tablesData?.run(adapter::setData)
    }

    private fun setTitle(tableGroup: TableGroup) {
        setToolbarTitle(getString(R.string.title_split_table))
        collapsing_toolbar_subtitle_text.text =
            "From table ${tableGroup.tableId}/${tableGroup.tablePart}"
    }
}
