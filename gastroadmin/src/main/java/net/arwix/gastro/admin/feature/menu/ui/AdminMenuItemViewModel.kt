package net.arwix.gastro.admin.feature.menu.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.domain.MenuUseCase
import net.arwix.mvi.SimpleIntentViewModel

class AdminMenuItemViewModel(private val menuUseCase: MenuUseCase) :
    SimpleIntentViewModel<AdminMenuItemViewModel.Action, AdminMenuItemViewModel.Result, AdminMenuItemViewModel.State>() {

    override var internalViewState: State = State()
    private val channelMenuGroupChange: Channel<String> = Channel(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            channelMenuGroupChange
                .consumeAsFlow()
                .filterNotNull()
                .flatMapLatest {
                    menuUseCase.getMenuFlow(it)
                }.collect {
                    notificationFromObserver(Result.SetMenu(it))
                }
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> {
        return liveData<Result> {
            //            when (action) {
////                is Action.SetMenu -> emit(Result.SetMenu(action.menuGroupData))
//            }
        }
    }

    fun setMenu(menu: MenuGroupData) {
        notificationFromObserver(Result.Clear)
        channelMenuGroupChange.offer(menu.name)
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.SetMenu -> {
                internalViewState.copy(menu = result.menuGroupData)
            }
//            is Result.UpdateMenuItem -> {
//                internalViewState.copy(menuGroups = result.menuGroups)
//            }
            Result.Clear -> State()
        }
    }

    suspend fun add(preMenuItem: MenuGroupData.PreMenuItem) {
        val menuGroup = internalViewState.menu ?: return
        menuUseCase.addMenuItem(menuGroup, preMenuItem)
    }

    suspend fun edit(
        oldPreMenuItem: MenuGroupData.PreMenuItem,
        preMenuItem: MenuGroupData.PreMenuItem
    ) {
        val menuGroup = internalViewState.menu ?: return
        menuUseCase.editMenuItem(menuGroup, oldPreMenuItem, preMenuItem)
    }

    fun delete(preMenuItem: MenuGroupData.PreMenuItem) {
        val menuGroup = internalViewState.menu ?: return
        viewModelScope.launch {
            menuUseCase.deleteMenuItem(menuGroup, preMenuItem)
        }
    }

    sealed class Action

    sealed class Result {
        object Clear : Result()
        data class SetMenu(val menuGroupData: MenuGroupData) : Result()
//        data class UpdateMenuItem(val menuGroups: List<MenuGroupData>) : Result()
    }


    data class State(
        val menu: MenuGroupData? = null
    )

}