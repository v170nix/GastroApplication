package net.arwix.gastro.boss.ui


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.findNavController
import kotlinx.android.synthetic.main.merge_profile_data.*
import kotlinx.android.synthetic.main.merge_toolbar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.common.setupToolbar
import net.arwix.gastro.boss.data.printer.PrinterRepository
import net.arwix.gastro.boss.ui.helper.ProfileHelper
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * A simple [Fragment] subclass.
 */
class SummaryBossFragment : Fragment(), CoroutineScope by MainScope() {

    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private val printerRepository: PrinterRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.liveState.observe(this, Observer(::renderProfile))
        requireActivity().onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_summary_boss, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main_toolbar.setupToolbar(findNavController(this), showHome = false, homeAsUp = false)
        profile_logout_button.setOnClickListener {
            profileViewModel.nonCancelableIntent(ProfileViewModel.Action.Logout)
        }
        launch {
            printerRepository.getOrUpdatePrinters()?.printers?.forEach {
                Log.e("printer", it.toString())
            }
        }
    }

    private fun renderProfile(state: ProfileViewModel.State) {
        Log.e("state profile summary", state.toString())
        if (state.account == null) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .popBackStack(R.id.signInBossFragment, true)
        } else {
            ProfileHelper.updateProfileInfo(this.view!!, state.account)
        }
    }
}
