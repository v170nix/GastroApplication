package net.arwix.gastro.library.menu

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import net.arwix.gastro.library.menu.MenuUtils.getNextRowAndCol
import net.arwix.gastro.library.menu.data.ColValue
import net.arwix.gastro.library.menu.data.MenuGridItem
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.RowValue
import net.arwix.gastro.library.menu.ui.MenuItemsGridAdapter

object MenuUtils {

    const val maxTableCols = 4

    fun getNextRowAndCol(menuGroup: MenuGroupData): Pair<RowValue, ColValue> {
        var maxRow = 1
        var maxCol = 1
        if (menuGroup.items.isNullOrEmpty()) return 1 to 1
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

    fun createGridLayoutManager(
        context: Context,
        adapter: MenuItemsGridAdapter
    ): GridLayoutManager {
        return GridLayoutManager(context, maxTableCols).apply {
            this.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val item = adapter.items[position]
                    return when (item) {
                        is MenuGridItem.Title -> maxTableCols
                        else -> 1
                    }
                }
            }
        }
    }
}

fun MenuGroupData.getNextCell() = getNextRowAndCol(this)