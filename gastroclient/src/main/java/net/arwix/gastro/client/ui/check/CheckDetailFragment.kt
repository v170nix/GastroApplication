package net.arwix.gastro.client.ui.check


import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.epson.epos2.Epos2Exception
import kotlinx.android.synthetic.main.fragment_check_detail.*
import kotlinx.coroutines.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.data.TableGroup
import org.koin.android.viewmodel.ext.android.sharedViewModel


class CheckDetailFragment : Fragment(), CoroutineScope by MainScope() {

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
        check_detail_print_button.setOnClickListener {
            launch(Dispatchers.IO) {
                runCatching {
                    checkDetailViewModel.print(requireContext().applicationContext)
                }.onSuccess {
                    launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "code: $it", Toast.LENGTH_LONG).apply {
                            this.setGravity(Gravity.CENTER, 0, 0)
                        }.show()
                    }
                }.onFailure {
                    launch(Dispatchers.Main) {
                        val mes = it.message ?: run {
                            if (it is Epos2Exception) {
                                it.errorStatus.toString()
                            } else it.toString()
                        }
                        Toast.makeText(
                            requireContext(),
                            "fail $mes",
                            Toast.LENGTH_LONG
                        ).apply {
                            this.setGravity(Gravity.CENTER, 0, 0)
                        }.show()
                    }
                }
            }
        }

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

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
