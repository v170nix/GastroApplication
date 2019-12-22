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
    val items: List<PreMenuItems>? = null,
    val metadata: Metadata
) : Parcelable {

    fun toMenuDoc(): MenuDoc =
        MenuDoc(printer, metadata.order, metadata.color, items)

    @Parcelize
    data class Metadata(
        val order: Int? = 1,
        @ColorInt val color: Int? = null,
        val updatedTime: Instant? = null
    ) : Parcelable

    @Parcelize
    data class PreMenuItems(
        val name: String? = null,
        val price: Int? = null,
        val color: Int? = null
    ) : Parcelable


}