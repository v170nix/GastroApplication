package net.arwix.gastro.client.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_main_client.*
import net.arwix.gastro.client.R
import net.arwix.gastro.client.ui.order.OrderViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MainClientFragment : Fragment() {

    private lateinit var navController: NavController
    private val orderViewModel: OrderViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        client_main_add_order_button.setOnClickListener {
            orderViewModel.clear()
            navController.navigate(R.id.orderTableFragment)
        }
    }
}
