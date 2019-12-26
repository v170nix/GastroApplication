package net.arwix.gastro.library.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TableGroup(val tableId: Int, val tablePart: Int = 1) : Parcelable {

    companion object {
        fun fromString(string: String) = string.split("-").let {
            TableGroup(it[0].toInt(), it[1].toInt())
        }
    }

    fun toDocId() = "$tableId-$tablePart"
    fun toPrintString() = "$tableId/$tablePart"

}