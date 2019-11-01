package net.arwix.gastro.boss.ui


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main_boss.*
import net.arwix.gastro.boss.R

/**
 * A simple [Fragment] subclass.
 */
class MainBossFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_boss, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start_button.setOnClickListener {
            requireActivity().startService(Intent(context, FirestoreService::class.java))
        }
        stop_button.setOnClickListener {
            requireActivity().startService(Intent(context, FirestoreService::class.java).apply {
                action = "STOP"
            })
        }
    }
}
