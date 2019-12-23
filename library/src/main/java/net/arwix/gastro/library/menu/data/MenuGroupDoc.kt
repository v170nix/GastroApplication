package net.arwix.gastro.library.menu.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import net.arwix.gastro.library.menu.MenuUtils
import org.threeten.bp.Instant

data class MenuGroupDoc(
    val printer: String? = null,
    val order: Int? = null,
    val color: Int? = null,
    val items: Map<MenuItemName, PreMenuItemValueDoc>? = null,
    @ServerTimestamp val updatedTime: Timestamp? = null
) {
    fun toMenuData(name: String) =
        MenuGroupData(
            name,
            printer,
            items?.map { it.value.toPreMenuItem(it.key) }?.sortedBy { it.row * MenuUtils.maxTableCols + it.col },
            MenuGroupData.Metadata(
                order, color,
                updatedTime?.run { Instant.ofEpochSecond(seconds) }
            )
        )

    data class PreMenuItemValueDoc(
        val price: Long = 0,
        val printer: String? = null,
        val color: Int? = null,
        val row: Int = 1,
        val col: Int = 1
    ) {
        fun toPreMenuItem(name: String) =
            MenuGroupData.PreMenuItem(name, price, printer, color, row, col)
    }
}

typealias MenuItemName = String