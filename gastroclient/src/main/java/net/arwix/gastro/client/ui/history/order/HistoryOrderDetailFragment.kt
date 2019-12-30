package net.arwix.gastro.client.ui.history.order


import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.epson.epos2.Epos2Exception
import kotlinx.android.synthetic.main.fragment_history_order_detail.*
import kotlinx.coroutines.*
import net.arwix.gastro.client.R
import net.arwix.gastro.client.feature.print.ui.PrintIntentService
import net.arwix.gastro.client.ui.history.check.HistoryCheckDetailAdapter
import net.arwix.gastro.client.ui.report.day.ReportDayUseCase
import net.arwix.gastro.library.common.CustomToolbarFragment
import net.arwix.gastro.library.common.setToolbarTitle
import net.arwix.gastro.library.data.TableGroup
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel


class HistoryOrderDetailFragment : CustomToolbarFragment(), CoroutineScope by MainScope() {

    override val idResToolbar: Int = R.id.app_main_toolbar

    private val orderDetailViewModel: HistoryOrderDetailViewModel by sharedViewModel()
    private val reportDayUseCase: ReportDayUseCase by inject()
    private lateinit var adapterHistory: HistoryCheckDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_order_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapterHistory = HistoryCheckDetailAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        setToolbarTitle("")

//        val priceTotal = 340 * 2
//        var itemString = createCharString(42, " ")
//        val nameString = "   2x Cappuccuno"
//        val formatter = NumberFormat.getCurrencyInstance()
//        val priceString = formatter.format(priceTotal / 100.0)
//        itemString = itemString.replaceRange(0, nameString.length, nameString)
//        itemString = itemString.replaceRange(itemString.length - priceString.length, itemString.length, priceString)
//        Log.e("print", itemString)

        with(history_order_detail_recycler_view) {
            layoutManager = linearLayoutManager
            itemAnimator = null
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    linearLayoutManager.orientation
                )
            )
            adapter = this@HistoryOrderDetailFragment.adapterHistory
        }
        orderDetailViewModel.liveState.observe(viewLifecycleOwner, Observer(this::render))
        orderDetailViewModel.nonCancelableIntent(HistoryOrderDetailViewModel.Action.GetLastOrder)

        test_print_2.setOnClickListener {
            PrintIntentService.startPrintOrder(
                requireContext().applicationContext,
                orderDetailViewModel.orderRef
            )
        }

        test_print_3.setOnClickListener {
            launch(Dispatchers.IO) {
                runCatching {
                    reportDayUseCase.printTest()
                }
            }
        }

        history_order_detail_print_button.setOnClickListener {
            launch(Dispatchers.IO) {
                runCatching {
                    orderDetailViewModel.print(requireContext().applicationContext)
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

    private fun render(state: HistoryOrderDetailViewModel.State) {
        state.orderData?.run {
            orderItems.run(adapterHistory::setItems)
            val t = table ?: return@run
            val tp = tablePart ?: return@run
            setTitle(TableGroup(t, tp))
        }
    }

    private fun setTitle(tableGroup: TableGroup) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            "Order table ${tableGroup.tableId}/${tableGroup.tablePart} "
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }


}
