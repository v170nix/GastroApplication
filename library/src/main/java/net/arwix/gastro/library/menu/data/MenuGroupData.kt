package net.arwix.gastro.library.menu.data

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Instant

@Keep
@Parcelize
data class MenuGroupData(
    val name: String,
    val printer: String?,
    val items: List<PreMenuItem>? = null,
    val metadata: Metadata
) : Parcelable {

    fun toMenuDoc(): MenuGroupDoc =
        MenuGroupDoc(
            printer,
            metadata.order,
            metadata.color,
//            items.groupBy {  }
            items?.associateBy(
                keySelector = { it.name as MenuItemName },
                valueTransform = { it.toPreMenuItemValueDoc() }
            ))

    @Parcelize
    data class Metadata(
        val order: Int? = 1,
        @ColorInt val color: Int? = null,
        val updatedTime: Instant? = null
    ) : Parcelable

    @Parcelize
    data class PreMenuItem(
        val name: MenuItemName? = null,
        val price: Long = 0,
        val printer: String? = null,
        val color: Int? = null,
        val position: Int = 100
    ) : Parcelable {
        fun toPreMenuItemValueDoc() = MenuGroupDoc.PreMenuItemValueDoc(
            price, printer, color, position
        )
    }


}