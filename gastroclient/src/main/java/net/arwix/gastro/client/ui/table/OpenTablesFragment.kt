package net.arwix.gastro.client.ui.table


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_open_tables.*
import net.arwix.gastro.client.R
import net.arwix.gastro.client.ui.order.OrderViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OpenTablesFragment : Fragment() {

    private lateinit var navController: NavController
    private val orderViewModel: OrderViewModel by sharedViewModel()
    private val openTablesViewModel: OpenTablesViewModel by sharedViewModel()
    private lateinit var adapter: OpenTablesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openTablesViewModel.liveState.observe(this, Observer(this::render))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_open_tables, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        open_tables_add_order_button.setOnClickListener {
            orderViewModel.clear()
            navController.navigate(R.id.orderTableFragment)
        }

        adapter = OpenTablesAdapter()
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
