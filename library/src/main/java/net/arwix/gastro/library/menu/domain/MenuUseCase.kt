package net.arwix.gastro.library.menu.domain

import net.arwix.gastro.library.await
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

    suspend fun addMenuGroup(menuGroupData: MenuGroupData) {
        repository.menuRef.document(menuGroupData.name).set(menuGroupData.toMenuDoc()).await()
    }

    suspend fun editMenuGroup(oldDocId: String, menuGroupData: MenuGroupData) {
        val newMenuDoc = repository.menuRef.document(menuGroupData.name)
        repository.menuRef.firestore.runTransaction {
            it.delete(repository.menuRef.document(oldDocId))
            it.set(newMenuDoc, menuGroupData.toMenuDoc())
        }.await()
    }

    suspend fun deleteMenuGroup(menuGroupData: MenuGroupData) {
        repository.menuRef.document(menuGroupData.name).delete().await()
    }

}