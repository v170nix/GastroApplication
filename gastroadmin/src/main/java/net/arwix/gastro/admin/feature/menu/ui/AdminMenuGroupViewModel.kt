package net.arwix.gastro.admin.feature.menu.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.domain.MenuUseCase
import net.arwix.mvi.SimpleIntentViewModel

class AdminMenuGroupViewModel(private val menuUseCase: MenuUseCase) :
    SimpleIntentViewModel<
            AdminMenuGroupViewModel.Action,
            AdminMenuGroupViewModel.Result,
            AdminMenuGroupViewModel.State>() {

    override var internalViewState: State = State()

    init {
        viewModelScope.launch {
            menuUseCase.getMenusFlow().collect {
                notificationFromObserver(Result.UpdateMenus(it))
            }
        }
    }

    suspend fun add(menuGroupData: MenuGroupData) {
        menuUseCase.addMenuGroup(menuGroupData)
    }

    suspend fun edit(oldMenuGroupData: MenuGroupData, newMenuGroupData: MenuGroupData) {
        menuUseCase.editMenuGroup(oldMenuGroupData, newMenuGroupData)
    }

    override fun dispatchAction(action: Action): LiveData<Result> {

        return liveData {
            when (action) {
                is Action.DeleteMenu -> menuUseCase.deleteMenuGroup(action.menuGroupData)
            }
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
        data class DeleteMenu(val menuGroupData: MenuGroupData) : Action()
    }


    sealed class Result {
        data class UpdateMenus(val menuGroups: List<MenuGroupData>) : Result()
    }


    data class State(
        val menuGroups: List<MenuGroupData>? = null
    )


}