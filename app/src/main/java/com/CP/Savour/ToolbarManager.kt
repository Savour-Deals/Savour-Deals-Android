package com.CP.Savour

import android.view.MenuItem
import android.view.View
import android.widget.Toolbar

class ToolbarManager constructor(private var builder: ToolbarFragment, private var container: View) {
    fun prepareToolbar() {
        if (builder.resId != ToolbarFragment.NO_TOOLBAR) {
            val toolbarFragment = container.findViewById(builder.resId) as Toolbar

            if (builder.title != -1) {
                toolbarFragment.setTitle(builder.title)
            }

            if (builder.menuId != -1) {
                toolbarFragment.inflateMenu(builder.menuId)
            }

            if (!builder.menuItems.isEmpty() && !builder.menuClicks.isEmpty()) {
                val menu = toolbarFragment.menu
                for ((index, menuItemId) in builder.menuItems.withIndex()) {
                    (menu.findItem(menuItemId) as MenuItem).setOnMenuItemClickListener(builder.menuClicks[index])
                }
            }
        }
    }
}
