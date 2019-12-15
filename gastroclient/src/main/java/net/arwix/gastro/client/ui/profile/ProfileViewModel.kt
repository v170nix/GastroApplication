package net.arwix.gastro.client.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import net.arwix.mvi.SimpleIntentViewModel

class ProfileViewModel :
    SimpleIntentViewModel<ProfileViewModel.Action, ProfileViewModel.Result, ProfileViewModel.State>() {

    override var internalViewState: State = State()

    override fun dispatchAction(action: Action): LiveData<Result> = liveData<Result> {
        when (action) {
            is Action.Login -> {
                if (checkPassword(action.password)) {
                    emit(Result.LoginAssert(action.password))
                } else {
                    emit(Result.LoginError(action.password))
                }
            }
            Action.Logout -> {
                emit(Result.Logout)
            }
        }

    }

    private fun checkPassword(password: Int): Boolean {
        return password in 1000..9999
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.LoginAssert -> internalViewState.copy(
                isLogin = true,
                userId = result.password,
                error = null
            )
            is Result.LoginError -> internalViewState.copy(
                isLogin = false,
                userId = result.password,
                error = true
            )
            Result.Logout -> internalViewState.copy(isLogin = false, userId = null, error = null)
        }
    }

    sealed class Action {
        data class Login(val password: Int) : Action()
        object Logout : Action()
    }

    sealed class Result {
        data class LoginAssert(val password: Int) : Result()
        data class LoginError(val password: Int) : Result()
        object Logout : Result()
    }

    data class State(
        val isLogin: Boolean = false,
        val userId: Int? = null,
        val error: Boolean? = null
    )
}