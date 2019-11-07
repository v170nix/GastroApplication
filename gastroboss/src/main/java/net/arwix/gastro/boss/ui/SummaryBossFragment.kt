package net.arwix.gastro.boss.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import kotlinx.android.synthetic.main.merge_profile_data.*
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.ui.helper.ProfileHelper
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * A simple [Fragment] subclass.
 */
class SummaryBossFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.liveState.observe(this, Observer(::renderProfile))
        requireActivity().onBackPressedDispatcher.addCallback(
            this, object: OnBackPressedCallback(true) {
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
        profile_logout_button.setOnClickListener {
            profileViewModel.nonCancelableIntent(ProfileViewModel.Action.Logout)
        }
    }

    private fun renderProfile(state: ProfileViewModel.State) {
        if (state.account == null) {
            findNavController(this).popBackStack(R.id.signInBossFragment, true)
        } else {
            ProfileHelper.updateProfileInfo(this.view!!, state.account)
        }
    }
}
