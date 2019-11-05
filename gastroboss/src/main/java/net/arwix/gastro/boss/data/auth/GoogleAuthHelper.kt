package net.arwix.gastro.boss.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

class GoogleAuthHelper(private val applicationContext: Context, serverClientId: String) {

    private val accessTokenStatusChannel = BroadcastChannel<String>(Channel.CONFLATED)
    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .requestServerAuthCode(serverClientId)
            .requestScopes(Scope(SCOPE_URL_CLOUD))
            .build()
    }
    private val gsi: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(applicationContext, gso)
    }

    fun getAccessTokenChannel() = accessTokenStatusChannel.openSubscription()

    private fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(applicationContext)
    }

    fun signIn(activity: Activity) {
        startActivityForResult(activity, gsi.signInIntent,
            RC_SIGN_IN, null)
    }

    fun doActivityResult(requestCode: Int, data: Intent?) {
        if (requestCode != RC_SIGN_IN) return
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            createAccessToken(account!!.serverAuthCode!!)
        } catch (e: ApiException) {
//            Log.e("Google sign in failed", e.toString())
        }
    }

    private fun createAccessToken(authCode: String) {

    }

    private companion object {
        private const val SCOPE_URL_CLOUD = "https://www.googleapis.com/auth/cloudprint"
        private const val RC_SIGN_IN = 375
    }
}