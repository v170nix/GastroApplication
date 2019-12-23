package net.arwix.gastro.library.menu

import net.arwix.gastro.library.menu.MenuUtils.getNextRowAndCol
import net.arwix.gastro.library.menu.data.ColValue
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.RowValue

object MenuUtils {

    const val maxTableCols = 5

    fun getNextRowAndCol(menuGroup: MenuGroupData): Pair<RowValue, ColValue> {
        var maxRow = 1
        var maxCol = 1
        menuGroup.items?.forEach {
            if (maxRow == it.row && maxCol < it.col) {
                maxCol = it.col
            }
            if (maxRow < it.row) {
                maxRow = it.row
                maxCol = it.col
            }
        }
        maxCol++
        if (maxCol > maxTableCols) {
            maxRow++
            maxCol = 1
        }

        return maxRow to maxCol
    }
}

fun MenuGroupData.getNextCell() = getNextRowAndCol(this)