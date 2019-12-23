package net.arwix.gastro.library.menu.domain

import androidx.annotation.ColorInt
import com.google.firebase.firestore.FieldValue
import net.arwix.gastro.library.await
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuGroupDoc
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
        val mergeGroup = newMenuGroupData.copy(
            items = updateMenuItemsData(
                oldMenuGroupData.items,
                oldMenuGroupData.printer,
                newMenuGroupData.printer,
                oldMenuGroupData.metadata.color,
                newMenuGroupData.metadata.color
            )
        )
        val newMenuDoc = repository.menuRef.document(mergeGroup.name)
        repository.menuRef.firestore.runTransaction {
            it.delete(repository.menuRef.document(oldMenuGroupData.name))
            it.set(newMenuDoc, mergeGroup.toMenuDoc())
        }.await()
    }

    private fun updateMenuItemsData(
        items: List<MenuGroupData.PreMenuItem>?,
        oldPrinterAddress: String?,
        newPrinterAddress: String?,
        @ColorInt oldColor: Int?,
        @ColorInt newColor: Int?
    ): List<MenuGroupData.PreMenuItem> {
        if (items == null) return listOf()
        if (oldPrinterAddress == newPrinterAddress && oldColor == newColor) return items
        return items.map {
            val printer = if (it.printer == oldPrinterAddress || it.printer == null) {
                newPrinterAddress
            } else it.printer
            val color = if (it.color == oldColor || it.color == null) {
                newColor
            } else it.color
            it.copy(
                printer = printer,
                color = color
            )
        }
    }

    suspend fun deleteMenuGroup(menuGroupData: MenuGroupData) {
        repository.menuRef.document(menuGroupData.name).delete().await()
    }

    suspend fun addMenuItem(menuGroupData: MenuGroupData, item: MenuGroupData.PreMenuItem) {
        repository.menuRef.firestore.runTransaction {
            val itemsMap = it
                .get(repository.menuRef.document(menuGroupData.name))
                .toObject(MenuGroupDoc::class.java)!!.items?.toMutableMap() ?: mutableMapOf()

            itemsMap[item.name!!] = item.toPreMenuItemValueDoc()

            it.update(
                repository.menuRef.document(menuGroupData.name),
                "items",
                itemsMap
            )
            it.update(
                repository.menuRef.document(menuGroupData.name),
                "updatedTime",
                FieldValue.serverTimestamp()
            )
        }.await()
    }

    suspend fun editMenuItem(
        menuGroupData: MenuGroupData,
        oldItem: MenuGroupData.PreMenuItem,
        item: MenuGroupData.PreMenuItem
    ) {
        repository.menuRef.firestore.runTransaction {
            val itemsMap = it
                .get(repository.menuRef.document(menuGroupData.name))
                .toObject(MenuGroupDoc::class.java)!!.items?.toMutableMap() ?: mutableMapOf()

            itemsMap.remove(oldItem.name)
            itemsMap[item.name!!] = item.toPreMenuItemValueDoc()

            it.update(
                repository.menuRef.document(menuGroupData.name),
                "items",
                itemsMap
            )
            it.update(
                repository.menuRef.document(menuGroupData.name),
                "updatedTime",
                FieldValue.serverTimestamp()
            )
        }.await()
    }

    suspend fun deleteMenuItem(
        menuGroupData: MenuGroupData,
        item: MenuGroupData.PreMenuItem
    ) {
        repository.menuRef.firestore.runTransaction {
            val itemsMap = it
                .get(repository.menuRef.document(menuGroupData.name))
                .toObject(MenuGroupDoc::class.java)!!.items?.toMutableMap() ?: mutableMapOf()
            itemsMap.remove(item.name)
            it.update(
                repository.menuRef.document(menuGroupData.name),
                "items",
                itemsMap
            )
            it.update(
                repository.menuRef.document(menuGroupData.name),
                "updatedTime",
                FieldValue.serverTimestamp()
            )
        }.await()
    }

}