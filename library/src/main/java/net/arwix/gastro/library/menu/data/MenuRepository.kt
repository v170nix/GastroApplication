package net.arwix.gastro.library.menu.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import net.arwix.gastro.library.await
import net.arwix.gastro.library.toFlow

class MenuRepository(val menuRef: CollectionReference) {

    private val menuGroupAsFlow: Flow<List<MenuGroupData>> =
        menuRef.orderBy("order").toFlow().map { snapshot: QuerySnapshot ->
            transformData(snapshot)
        }


    fun getMenusAsFlow() = menuGroupAsFlow
    fun getMenuAsFlow(name: String) = menuRef.document(name).toFlow().map {
        it.toObject(MenuGroupDoc::class.java)?.toMenuData(it.id)
    }.filterNotNull()

    suspend fun getMenus() = transformData(menuRef.orderBy("order").get().await()!!)

    private fun transformData(snapshot: QuerySnapshot) =
        snapshot.map { it.toObject(MenuGroupDoc::class.java).toMenuData(it.id) }


}