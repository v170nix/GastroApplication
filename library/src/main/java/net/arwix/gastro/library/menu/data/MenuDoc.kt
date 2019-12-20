package net.arwix.gastro.library.menu.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import org.threeten.bp.Instant

data class MenuDoc(
    val printer: String? = null,
    val order: Int? = null,
    val color: Int? = null,
    val items: List<MenuGroupData.PreMenuItems>? = null,
    @ServerTimestamp val updatedTime: Timestamp? = null
) {
    fun toMenuData(name: String) =
        MenuGroupData(
            name, printer, items,
            MenuGroupData.Metadata(
                order, color,
                updatedTime?.run { Instant.ofEpochSecond(seconds) }
            )
        )
}