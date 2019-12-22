package net.arwix.gastro.library.menu.domain

import com.google.firebase.firestore.FieldValue
import net.arwix.gastro.library.await
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuRepository

class MenuUseCase(private val repository: MenuRepository) {

    private var menuGroups: List<MenuGroupData>? = null

    suspend fun getMenus(isForce: Boolean = false): List<MenuGroupData> {
        val innerMenu = if (isForce) null else menuGroups
        return innerMenu ?: repository.getData().also {
            menuGroups = innerMenu
        }
    }

    fun getMenusFlow() = repository.getMenusFlow()

    fun getMenuFlow(name: String) = repository.getMenuFlow(name)

    suspend fun addMenuGroup(menuGroupData: MenuGroupData) {
        repository.menuRef.document(menuGroupData.name).set(menuGroupData.toMenuDoc()).await()
    }

    suspend fun editMenuGroup(
        oldMenuGroupData: MenuGroupData,
        newMenuGroupData: MenuGroupData
    ) {
        val mergeGroup = newMenuGroupData.copy(items = oldMenuGroupData.items)
        val newMenuDoc = repository.menuRef.document(mergeGroup.name)
        repository.menuRef.firestore.runTransaction {
            it.delete(repository.menuRef.document(oldMenuGroupData.name))
            it.set(newMenuDoc, mergeGroup.toMenuDoc())
        }.await()
    }

    suspend fun deleteMenuGroup(menuGroupData: MenuGroupData) {
        repository.menuRef.document(menuGroupData.name).delete().await()
    }

    suspend fun addMenuItem(menuGroupData: MenuGroupData, item: MenuGroupData.PreMenuItem) {
        repository.menuRef.firestore.runTransaction {
            it.update(
                repository.menuRef.document(menuGroupData.name),
                "items",
                FieldValue.arrayUnion(item)
            )
            it.update(
                repository.menuRef.document(menuGroupData.name),
                "updatedTime",
                FieldValue.serverTimestamp()
            )
        }.await()
    }

}