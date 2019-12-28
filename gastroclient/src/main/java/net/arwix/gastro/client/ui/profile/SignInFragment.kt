package net.arwix.gastro.client.ui.profile


import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_sign_in.*
import net.arwix.gastro.client.R
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SignInFragment : Fragment() {

    private val profileViewModel by sharedViewModel<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_sign_in, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileViewModel.liveState.observe(viewLifecycleOwner, Observer(this::renderProfile))
        signin_button.setOnClickListener { login() }
//        val text = "<small style=\"float:left\"><font color='#000000'>" +
//                "Mon-Sat 5:00 pm" + "</font> </small>"+ "<br/>" +
//                "<small style=\"float:right\"> <font color='#000000'>" + "Closed on Sunday" +
//                "</font> </small>"
//
//        signin_button.text = Html.fromHtml(text)
        password_text_edit.setOnEditorActionListener { v, actionId, event ->
            if ((event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) login()
            false
        }
    }

    override fun onStart() {
        super.onStart()
        password_text_edit.setText("")
        password_input_layout.error = null
    }

    private fun login() {
        profileViewModel.intent(
            ProfileViewModel.Action.Login(
                password_text_edit.editableText.toString().toIntOrNull() ?: -1
            )
        )
    }


    private fun renderProfile(state: ProfileViewModel.State) {
        if (state.error != null) {
            password_input_layout.error = null
            if (password_text_edit.editableText.toString() != "") {
                password_input_layout.isErrorEnabled = true
                password_input_layout.error = getString(R.string.error_sign_in)
            }
        }
    }


}

