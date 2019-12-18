package net.arwix.gastro.client.ui.check


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_check_detail.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.data.TableGroup
import org.koin.android.viewmodel.ext.android.sharedViewModel


class CheckDetailFragment : Fragment() {

    private val checkDetailViewModel: CheckDetailViewModel by sharedViewModel()
    private lateinit var adapter: CheckDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_check_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CheckDetailAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        with(check_detail_list_recycler_view) {
            layoutManager = linearLayoutManager
            itemAnimator = null
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    linearLayoutManager.orientation
                )
            )
            adapter = this@CheckDetailFragment.adapter
        }
        checkDetailViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        checkDetailViewModel.nonCancelableIntent(CheckDetailViewModel.Action.GetLastCheck)
    }

    private fun render(state: CheckDetailViewModel.State) {
        state.checkData?.run {
            checkItems?.run(adapter::setItems)
            val t = table ?: return@run
            val tp = tablePart ?: return@run
            setTitle(TableGroup(t, tp))
        }
    }

    private fun setTitle(tableGroup: TableGroup) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            "Check table ${tableGroup.tableId}/${tableGroup.tablePart} "
    }
}
