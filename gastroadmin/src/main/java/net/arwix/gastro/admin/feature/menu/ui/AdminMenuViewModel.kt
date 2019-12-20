package net.arwix.gastro.admin.feature.menu.ui

import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.domain.MenuUseCase
import net.arwix.mvi.SimpleIntentViewModel

class AdminMenuViewModel(private val menuUseCase: MenuUseCase) :
    SimpleIntentViewModel<AdminMenuViewModel.Action, AdminMenuViewModel.Result, AdminMenuViewModel.State>() {

    override var internalViewState: State = State()

    init {
        viewModelScope.launch {
            menuUseCase.getFlow().collect {
                notificationFromObserver(Result.UpdateMenus(it))
            }
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> {

        return liveData<Result> {

        }

    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.UpdateMenus -> {
                internalViewState.copy(menuGroups = result.menuGroups)
            }
        }
    }

    sealed class Action {
        data class DeleteMenu(val menuGroupData: MenuGroupData)
        data class AddMenu(
            val name: String,
            val printer: String?,
            @ColorInt val color: Int? = null
        )

        data class UpdateMenu(
            val name: String,
            val printer: String?,
            @ColorInt val color: Int? = null
        )
    }


    sealed class Result {
        data class UpdateMenus(val menuGroups: List<MenuGroupData>) : Result()
    }


    data class State(
        val menuGroups: List<MenuGroupData>? = null
    )


}