package net.arwix.gastro.client.ui.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_in.*
import net.arwix.gastro.client.R
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SignInFragment : Fragment() {

    private val profileViewModel by sharedViewModel<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileViewModel.liveState.observe(viewLifecycleOwner, Observer(this::renderProfile))
        signin_button.setOnClickListener {
            profileViewModel.intent(
                ProfileViewModel.Action.Login(
                    password_text_edit.editableText.toString().toInt()
                )
            )
        }
    }

    private fun renderProfile(state: ProfileViewModel.State) {
        if (state.isLogin) {
            findNavController(this).navigate(R.id.mainClientFragment)
        }
        if (state.error != null) {
            password_input_layout.error = null
            password_input_layout.isErrorEnabled = true
            password_input_layout.error = "wrong password"
        }
    }


}

