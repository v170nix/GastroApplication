package net.arwix.gastro.boss.ui.helper

import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.synthetic.main.merge_profile_data.view.*

object ProfileHelper {

    fun updateProfileInfo(view: View, account: GoogleSignInAccount) {
        view.profile_display_name.text = account.displayName
        view.profile_email.text = account.email
        @Suppress("DEPRECATION")
        view.profile_image.setImageURI(account.photoUrl)
    }

}