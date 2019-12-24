package net.arwix.gastro.library.menu.data

sealed class MenuGridItem {
    data class Title(val value: MenuGroupData) : MenuGridItem() {
        override fun getViewType() = VIEW_TYPE_TITLE
    }

    data class Item(val menu: MenuGroupData, val value: MenuGroupData.PreMenuItem) :
        MenuGridItem() {
        override fun getViewType() = VIEW_TYPE_ITEM
    }

    object Empty : MenuGridItem() {
        override fun getViewType() = VIEW_TYPE_EMPTY
    }

    abstract fun getViewType(): Int

    companion object {
        const val VIEW_TYPE_TITLE = 10
        const val VIEW_TYPE_ITEM = 20
        const val VIEW_TYPE_EMPTY = 30
    }
}