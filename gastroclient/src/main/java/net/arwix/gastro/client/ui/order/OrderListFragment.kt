package net.arwix.gastro.client.ui.order


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_order_list.*

import net.arwix.gastro.client.R
import org.koin.android.viewmodel.ext.android.sharedViewModel

class OrderListFragment : Fragment() {

    private val orderViewModel: OrderViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderViewModel.liveState.observe(this, ::render)
        order_list_add_button.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
                R.id.orderListAddFragment
            )
        }
    }

    private fun render(state: OrderViewModel.State) {

    }
}
