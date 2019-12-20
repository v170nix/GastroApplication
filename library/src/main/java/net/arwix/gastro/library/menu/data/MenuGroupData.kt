package net.arwix.gastro.library.menu.data

import androidx.annotation.ColorInt
import org.threeten.bp.Instant

data class MenuGroupData(
    val name: String,
    val printer: String?,
    val items: List<PreMenuItems>? = null,
    val metadata: Metadata
) {
    data class Metadata(
        val order: Int? = 1, @ColorInt val color: Int? = null,
        val updatedTime: Instant? = null
    )

    data class PreMenuItems(val name: String? = null, val price: Int? = null)
}