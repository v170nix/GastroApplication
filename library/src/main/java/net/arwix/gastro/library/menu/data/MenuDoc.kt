package net.arwix.gastro.library.menu.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import org.threeten.bp.Instant

data class MenuDoc(
    val printer: String? = null,
    val order: Int? = null,
    val color: Int? = null,
    val items: Map<MenuItemName, PreMenuItemValueDoc>? = null,
    @ServerTimestamp val updatedTime: Timestamp? = null
) {
    fun toMenuData(name: String) =
        MenuGroupData(
            name, printer, items?.map { it.value.toPreMenuItem(it.key) }?.sortedBy { it.position },
            MenuGroupData.Metadata(
                order, color,
                updatedTime?.run { Instant.ofEpochSecond(seconds) }
            )
        )

    data class PreMenuItemValueDoc(
        val price: Int = 0,
        val printer: String? = null,
        val color: Int? = null,
        val position: Int = 100
    ) {
        fun toPreMenuItem(name: String) =
            MenuGroupData.PreMenuItem(name, price, printer, color, position)
    }
}

typealias MenuItemName = String
typealias MenuGroupName = String