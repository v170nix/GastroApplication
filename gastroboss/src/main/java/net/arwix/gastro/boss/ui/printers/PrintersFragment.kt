package net.arwix.gastro.boss.ui.printers

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_printer_list.*
import kotlinx.android.synthetic.main.fragment_printer_list.view.*
import kotlinx.android.synthetic.main.merge_toolbar.*
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.common.setupToolbar
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PrintersFragment : Fragment() {

    private val printersViewModel: PrintersViewModel by sharedViewModel()
    private lateinit var adapter: PrintersRecyclerViewAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        printersViewModel.liveState.observe(this, Observer(::render))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_printer_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PrintersRecyclerViewAdapter()
        with(view.printers_list_recycler_view) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PrintersFragment.adapter
        }
        main_toolbar.setupToolbar(
            findNavController(this),
            showHome = true,
            homeAsUp = true,
            navigationOnClickListener = View.OnClickListener {
                findNavController(this).navigateUp()
            }
        )
        printers_submit_selected_button.setOnClickListener {
            printersViewModel.submit(adapter.getSelectedItems())
            findNavController(this).navigateUp()
        }
    }

    private fun render(state: PrintersViewModel.State) {
        Log.e("printers state", state.toString())
        if (state.printers?.printers != null) {
            adapter.setData(state.printers, state.selectedPrinters)
        }
    }

}
