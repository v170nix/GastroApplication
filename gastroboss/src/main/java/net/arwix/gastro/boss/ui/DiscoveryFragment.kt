package net.arwix.gastro.boss.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import com.epson.epos2.Epos2Exception
import com.epson.epos2.discovery.Discovery
import com.epson.epos2.discovery.DiscoveryListener
import com.epson.epos2.discovery.FilterOption
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.ShowMsg.showException


class DiscoveryFragment : Fragment(), View.OnClickListener, AdapterView.OnItemClickListener {

    private val mContext: Context? = null
    private var mPrinterList: ArrayList<HashMap<String, String>>? = null
    private var mPrinterListAdapter: SimpleAdapter? = null
    private var mFilterOption: FilterOption? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discovery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val button: Button = view.findViewById(R.id.btnRestart) as Button
        button.setOnClickListener(this)

        mPrinterList = ArrayList()
        mPrinterListAdapter = SimpleAdapter(
            requireContext(),
            mPrinterList,
            R.layout.list_at,
            arrayOf("PrinterName", "Target"),
            intArrayOf(R.id.PrinterName, R.id.Target)
        )
        val list: ListView = view.findViewById(R.id.lstReceiveData) as ListView
        list.adapter = mPrinterListAdapter
        list.onItemClickListener = this

        mFilterOption = FilterOption()
        mFilterOption?.deviceType = Discovery.TYPE_PRINTER
        mFilterOption?.epsonFilter = Discovery.FILTER_NAME
        try {
            Discovery.start(requireContext(), mFilterOption, mDiscoveryListener)
        } catch (e: Exception) {
            showException(e, "start", mContext!!)
        }
    }

    private val mDiscoveryListener: DiscoveryListener =
        DiscoveryListener { deviceInfo ->
            requireActivity().runOnUiThread {
                val item =
                    HashMap<String, String>()
                item["PrinterName"] = deviceInfo.deviceName
                item["Target"] = deviceInfo.target
                mPrinterList!!.add(item)
                mPrinterListAdapter!!.notifyDataSetChanged()
            }
        }

    override fun onClick(v: View?) {
        restartDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        while (true) {
            try {
                Discovery.stop()
                break
            } catch (e: Epos2Exception) {
                if (e.errorStatus != Epos2Exception.ERR_PROCESSING) {
                    break
                }
            }
        }
        mFilterOption = null
    }

    private fun restartDiscovery() {
        while (true) {
            try {
                Discovery.stop()
                break
            } catch (e: Epos2Exception) {
                if (e.errorStatus != Epos2Exception.ERR_PROCESSING) {
                    showException(e, "stop", mContext!!)
                    return
                }
            }
        }
        mPrinterList!!.clear()
        mPrinterListAdapter!!.notifyDataSetChanged()
        try {
            Discovery.start(requireContext(), mFilterOption, mDiscoveryListener)
        } catch (e: java.lang.Exception) {
            showException(e, "stop", mContext!!)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }
}







