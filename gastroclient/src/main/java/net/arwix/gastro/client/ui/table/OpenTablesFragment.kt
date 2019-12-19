package net.arwix.gastro.client.ui.table


import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_open_tables.*
import net.arwix.gastro.client.R
import net.arwix.gastro.client.ui.order.OrderViewModel
import net.arwix.gastro.client.ui.pay.PayViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OpenTablesFragment : Fragment() {

    private val orderViewModel: OrderViewModel by sharedViewModel()
    private val openTablesViewModel: OpenTablesViewModel by sharedViewModel()
    private val payViewModel: PayViewModel by sharedViewModel()
    private lateinit var adapter: OpenTablesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_open_tables, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle()
        openTablesViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        open_tables_add_order_button.setOnClickListener {
            orderViewModel.clear()
            findNavController(this).navigate(R.id.orderTableFragment)
        }

        adapter = OpenTablesAdapter(
            onItemClick = {
                payViewModel.setTable(it)
                findNavController(this).navigate(R.id.payListFragment)
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

    private fun setTitle() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Open tables"
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.standart_menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        super.onCreateOptionsMenu(menu, inflater)
    }
}
