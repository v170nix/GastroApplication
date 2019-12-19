package net.arwix.gastro.client.common

import android.view.Menu
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import net.arwix.gastro.client.R

object MenuHelper {

    fun updateVisibleMenu(fragmentId: Int, menu: Menu) {
        when (fragmentId) {
            R.id.payListFragment -> {
                menu.forEach { it.isVisible = true }
            }
            R.id.signInFragment -> hideMenu(menu)
            R.id.orderAddTableFragment -> hideMenu(menu)
            R.id.orderListAddItemFragment -> hideMenu(menu)
            else -> menu.forEach {
                it.isVisible = it.itemId != R.id.menu_pay_add_items
            }
        }
    }

    fun updateActionBar(activity: AppCompatActivity, fragmentId: Int, actionBar: ActionBar) {
        when (fragmentId) {
            R.id.openTablesFragment -> {
                actionBar.title = activity.getString(R.string.title_open_tables)
            }
        }

        when (fragmentId) {
            R.id.orderAddTableFragment -> {
                actionBar.subtitle = activity.getString(R.string.subtitle_order_add_table)
            }
            R.id.orderListFragment -> {
            }
            else -> {
                actionBar.subtitle = null
            }
        }
    }

    private fun hideMenu(menu: Menu) {
        menu.forEach { it.isVisible = false }
    }

}