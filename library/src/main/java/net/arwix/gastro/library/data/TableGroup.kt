package net.arwix.gastro.library.data

data class TableGroup(val tableId: Int, val tablePart: Int = 1) {

    companion object {
        fun fromString(string: String) = string.split("-").let {
            TableGroup(it[0].toInt(), it[1].toInt())
        }
    }

    fun toDocId() = "$tableId-$tablePart"
    fun toPrintString() = "$tableId/$tablePart"

}