package net.arwix.gastro.boss.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import net.arwix.extension.runWeak
import net.arwix.gastro.boss.data.auth.AccessTokenProvider
import net.arwix.gastro.library.await
import net.arwix.mvi.SimpleIntentViewModel
import java.lang.ref.WeakReference

class ProfileViewModel(
    private val applicationContext: Context,
    private val googleSignInClient: GoogleSignInClient,
    private val accessTokenProvider: AccessTokenProvider
) :
    SimpleIntentViewModel<ProfileViewModel.Action, ProfileViewModel.Result, ProfileViewModel.State>() {

    override var internalViewState = State(null)

    init {
        notificationFromObserver(
            Result.UpdateAccount(
                GoogleSignIn.getLastSignedInAccount(
                    applicationContext
                )
            )
        )
    }

    override fun dispatchAction(action: Action): LiveData<Result> = liveData {
        when (action) {
            Action.Logout -> {
                runCatching { googleSignInClient.signOut().await() }
                emit(
                    Result.UpdateAccount(GoogleSignIn.getLastSignedInAccount(applicationContext))
                )
            }
            is Action.LoginStart -> {
                action.ref.runWeak {
                    startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
                }
            }
            is Action.LoginDoActivityResult -> {
                if (action.requestCode != RC_SIGN_IN) return@liveData
                runCatching {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(action.data)
                        .getResult(ApiException::class.java)
                    accessTokenProvider.updateToken(account!!.serverAuthCode)
                }.onFailure {
                    emit(
                        Result.LoginError(
                            GoogleSignIn.getLastSignedInAccount(
                                applicationContext
                            ), it
                        )
                    )
                }.onSuccess {
                    emit(
                        Result.UpdateAccount(
                            GoogleSignIn.getLastSignedInAccount(
                                applicationContext
                            )
                        )
                    )
                }
            }
        }
    }


    override fun reduce(result: Result): State {
        Log.e("reduce", result.toString())
        return when (result) {
            is Result.UpdateAccount -> internalViewState.copy(
                account = result.account,
                error = null
            )
            is Result.LoginError -> internalViewState.copy(result.account, result.error)
        }
    }

    sealed class Action {
        data class LoginStart(val ref: WeakReference<Fragment>) : Action()
        data class LoginDoActivityResult(val requestCode: Int, val data: Intent?) : Action()
        object Logout : Action()
    }


    sealed class Result {
        data class UpdateAccount(val account: GoogleSignInAccount?) : Result()
        data class LoginError(val account: GoogleSignInAccount?, val error: Throwable?) : Result()
    }

    data class State(
        val account: GoogleSignInAccount?,
        val error: Throwable? = null
    )

    private companion object {
        private const val RC_SIGN_IN = 489
    }

}