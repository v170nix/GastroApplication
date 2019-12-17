package net.arwix.gastro.client.ui.profile


import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_in.*
import net.arwix.gastro.client.R
import net.arwix.gastro.library.common.hideKeyboard
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SignInFragment : Fragment() {

    private val profileViewModel by sharedViewModel<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        profileViewModel.liveState.observe(viewLifecycleOwner, Observer(this::renderProfile))
        signin_button.setOnClickListener { login() }
        password_text_edit.setOnEditorActionListener { v, actionId, event ->
            if ((event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) login()
            false
        }
    }

    private fun login() {
        profileViewModel.intent(
            ProfileViewModel.Action.Login(
                password_text_edit.editableText.toString().toIntOrNull() ?: -1
            )
        )
    }

    private fun renderProfile(state: ProfileViewModel.State) {
        if (state.isLogin) {
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.openTablesFragment, true).build()
            requireActivity().hideKeyboard()
            findNavController(this).navigate(R.id.openTablesFragment, null, navOptions)
        }
        if (state.error != null) {
            password_input_layout.error = null
            password_input_layout.isErrorEnabled = true
            password_input_layout.error = "wrong password"
        }
    }


}
