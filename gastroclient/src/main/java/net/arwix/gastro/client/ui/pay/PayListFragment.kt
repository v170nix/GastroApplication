package net.arwix.gastro.client.ui.pay


import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_pay_list.*
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.client.R
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.library.data.TableGroup
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.NumberFormat

class PayListFragment : Fragment() {

    private val payViewModel: PayViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private lateinit var adapter: PayListItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        payViewModel.liveState.observe(this, Observer(this::render))
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pay_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PayListItemAdapter { type, payOrderItem, delta ->
            payViewModel.nonCancelableIntent(
                PayViewModel.Action.ChangePayCount(
                    type,
                    payOrderItem,
                    delta
                )
            )
            //  listener?.invoke(itemView, type, payOrderItem, delta)
        }
        val linearLayoutManager = LinearLayoutManager(requireContext())
        with(pay_list_order_recycler_view) {
            layoutManager = linearLayoutManager
            itemAnimator = null
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    context,
                    linearLayoutManager.orientation
                )
            )
            adapter = this@PayListFragment.adapter
        }

        pay_list_submit_button.setOnClickListener {
            val userId = profileViewModel.liveState.value?.userId ?: return@setOnClickListener
            payViewModel.nonCancelableIntent(PayViewModel.Action.CheckOut(userId))
        }

    }

    private fun render(state: PayViewModel.State) {
        updateTextAndVisiblePayButton(payViewModel.liveState.value)
        state.tableGroup?.run(this::setTitle)
        state.summaryData?.run {
            adapter.setItems(this)
        }
        if (state.isCloseTableGroup) {
            Log.e("isClose", "3")
            Toast.makeText(
                requireContext(),
                "close table ${state.tableGroup?.toPrintString()}",
                Toast.LENGTH_LONG
            )
                .apply {
                    this.setGravity(Gravity.CENTER, 0, 0)
                }
                .show()
            findNavController(this).navigateUp()
        }
    }

    private fun setTitle(tableGroup: TableGroup) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            "Table ${tableGroup.tableId}/${tableGroup.tablePart} "
    }

    private fun getPayCountAndPrice(state: PayViewModel.State?): Pair<Long, Int>? {
        state ?: return null
        val payOrderItems = state.summaryData ?: return null
        var count = 0
        var price = 0L
        payOrderItems.values.forEach { payOrderItem ->
            payOrderItem.forEach {
                if (it.payCount > 0) {
                    count += it.payCount
                    price += it.orderItem.price * it.payCount
                }
            }
        }
        return price to count
    }

    private fun updateTextAndVisiblePayButton(state: PayViewModel.State?) {
        getPayCountAndPrice(state)?.let { (price, count) ->
            if (price > 0) {
                pay_list_submit_button.visible()
            } else {
                pay_list_submit_button.gone()
                return
            }
            val formatter = NumberFormat.getCurrencyInstance()
            pay_list_submit_button.text = "To pay: ${formatter.format(price / 100.0)}"
        } ?: run {
            pay_list_submit_button.gone()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.open_tables_menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_tables_menu_add_all_to_pay -> {
                payViewModel.nonCancelableIntent(PayViewModel.Action.AddAllItemsToPay)
                return true
            }
        }
        return false
    }


}
