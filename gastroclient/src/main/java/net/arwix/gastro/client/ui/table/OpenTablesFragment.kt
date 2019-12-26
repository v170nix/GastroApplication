package net.arwix.gastro.client.ui.table


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_open_tables.*
import net.arwix.gastro.client.R
import net.arwix.gastro.client.ui.order.OrderViewModel
import net.arwix.gastro.client.ui.pay.PayViewModel
import net.arwix.gastro.library.common.navigate
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OpenTablesFragment : Fragment() {

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private val openTablesViewModel: OpenTablesViewModel by sharedViewModel()
    private val payViewModel: PayViewModel by sharedViewModel()
    private lateinit var adapter: OpenTablesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_open_tables, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openTablesViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        open_tables_add_order_button.setOnClickListener {
            orderViewModel.clear()
            findNavController().navigate(R.id.orderAddTableFragment)
        }

        adapter = OpenTablesAdapter(
            onItemClick = {
                payViewModel.setTable(it)
                findNavController().navigate(R.id.payListFragment)
            },
            onAddOrderClick = {
                orderViewModel.selectTable(it)
                OpenTablesFragmentDirections.actionGlobalOrderListFragment(true)
                    .navigate(this)
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
