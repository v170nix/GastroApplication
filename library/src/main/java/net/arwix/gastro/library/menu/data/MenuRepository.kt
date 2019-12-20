package net.arwix.gastro.library.menu.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.arwix.gastro.library.await
import net.arwix.gastro.library.toFlow

class MenuRepository(private val menuRef: CollectionReference) {

    private val menuGroupAsFlow: Flow<List<MenuGroupData>> =
        menuRef.orderBy("order").toFlow().map { snapshot: QuerySnapshot ->
            transformData(snapshot)
        }

    fun getFlow() = menuGroupAsFlow

    suspend fun getData() = transformData(menuRef.orderBy("order").get().await()!!)

    private fun transformData(snapshot: QuerySnapshot) =
        snapshot.map { it.toObject(MenuDoc::class.java).toMenuData(it.id) }


}