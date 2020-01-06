package net.arwix.gastro.client.ui.table


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_open_tables.*
import net.arwix.gastro.client.R
import net.arwix.gastro.client.feature.order.ui.OrderViewModel
import net.arwix.gastro.client.feature.table.ui.OpenTableViewModel
import net.arwix.gastro.library.common.CustomToolbarFragment
import net.arwix.gastro.library.common.setToolbarTitle
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OpenTablesFragment : CustomToolbarFragment() {

    override val idResToolbar: Int = R.id.app_main_toolbar

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private val openTablesViewModel: OpenTablesViewModel by sharedViewModel()
    private val openTableViewModel: OpenTableViewModel by sharedViewModel()
    private lateinit var adapter: OpenTablesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_open_tables, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(getString(R.string.title_open_tables))
        openTablesViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        open_tables_add_order_button.setOnClickListener {
            orderViewModel.clear()
            orderViewModel.isAnimateBigButton = true
            findNavController().navigate(R.id.orderAddTableFragment)
        }

        adapter = OpenTablesAdapter(
            onItemClick = {
                openTableViewModel.setTable(it)
                findNavController().navigate(R.id.payListFragment)
            },
            onAddOrderClick = {
                orderViewModel.selectTable(it)
                orderViewModel.isAnimateBigButton = true
                findNavController().navigate(OpenTablesFragmentDirections.actionGlobalOrderListFragment())
            }
        )
        with(open_tables_recycler_view) {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            itemAnimator = null
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
            adapter = this@OpenTablesFragment.adapter
        }
    }

    private fun render(state: OpenTablesViewModel.State) {
        state.tablesData?.run(adapter::setData)
    }

}
