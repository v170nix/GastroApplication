package net.arwix.gastro.client.ui.pay


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_pay_list.*
import net.arwix.extension.gone
import net.arwix.extension.visible
import net.arwix.gastro.client.R
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.text.NumberFormat

class PayListFragment : Fragment() {

    private val payViewModel: PayViewModel by sharedViewModel()
    private lateinit var adapter: PayListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        payViewModel.liveState.observe(this, Observer(this::render))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pay_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter =
            PayListAdapter { part: PayViewModel.StatePart, type: String, payOrderItem: PayViewModel.PayOrderItem, delta: Int ->
                payViewModel.nonCancelableIntent(
                    PayViewModel.Action.ChangePayCount(
                        part, type, payOrderItem, delta
                    )
                )
            }
        pay_list_pager.adapter = this.adapter
        val mediator = TabLayoutMediator(pay_list_tab_layout, pay_list_pager) { tab, position ->
            tab.text = "part №${position + 1}"
        }
        mediator.attach()
        pay_list_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTextAndVisiblePayButton(position, payViewModel.liveState.value)
            }

        })
    }

    private fun render(state: PayViewModel.State) {
        updateTextAndVisiblePayButton(pay_list_pager.currentItem, payViewModel.liveState.value)
        state.tableId?.run(this::setTitle)
        state.parts?.run {
            adapter.setPartList(this)
            if (this.size == 1) {
                pay_list_tab_layout.gone()
            } else {
                pay_list_tab_layout.visible()
            }
        }
    }

    private fun setTitle(tableId: Int) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Table $tableId"
    }

    private fun getPayCountAndPrice(position: Int, state: PayViewModel.State?): Pair<Long, Int>? {
        state ?: return null
        val parts = state.parts ?: return null
        val part = parts.getOrNull(position) ?: return null
        var count = 0
        var price = 0L
        part.payOrderData.payOrderItems.values.forEach { payOrderItems ->
            payOrderItems.forEach {
                if (it.payCount > 0) {
                    count += it.payCount
                    price += it.orderItem.price * it.payCount
                }
            }
        }
        return price to count
    }

    private fun updateTextAndVisiblePayButton(position: Int, state: PayViewModel.State?) {
        getPayCountAndPrice(position, state)?.let { (price, count) ->
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

    private fun checkVisiblePayButton(position: Int, state: PayViewModel.State?): Boolean {
        state ?: return false
        val parts = state.parts ?: return false
        val part = parts.getOrNull(position) ?: return false
        part.payOrderData.payOrderItems.values.forEach {
            it.forEach {
                if (it.payCount > 0) return true
            }
        }
        return false
    }


}
