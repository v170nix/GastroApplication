package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import org.threeten.bp.Instant

data class MenuDoc(
    val printer: String? = null,
    val order: Int? = null,
    val color: Int? = null,
    val items: List<MenuData.PreMenuItems>? = null,
    @ServerTimestamp val updatedTime: Timestamp? = null
) {
    fun toMenuData(name: String) =
        MenuData(name, printer, items,
            MenuData.Metadata(order, color,
                updatedTime?.run { Instant.ofEpochSecond(seconds) }
            )
        )
}