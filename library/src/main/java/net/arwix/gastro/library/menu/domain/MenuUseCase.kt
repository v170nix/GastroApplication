package net.arwix.gastro.library.menu.domain

import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuRepository

class MenuUseCase(private val repository: MenuRepository) {

    private var menuGroups: List<MenuGroupData>? = null

    suspend fun getMenu(): List<MenuGroupData> {
        val innerMenu = menuGroups
        return innerMenu ?: repository.getData().also {
            menuGroups = innerMenu
        }
    }

    fun getFlow() = repository.getFlow()


}