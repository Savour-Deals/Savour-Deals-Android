package com.CP.Savour

import android.support.annotation.IdRes
import android.support.annotation.MenuRes
import android.support.annotation.StringRes
import android.view.MenuItem

class ToolbarFragment(@IdRes val resId: Int,
                      @StringRes val title: Int,
                      @MenuRes val menuId: Int,
                      val menuItems: MutableList<Int>,
                      val menuClicks: MutableList<MenuItem.OnMenuItemClickListener?>) {

    companion object {
        @JvmField val NO_TOOLBAR = -1
    }

    class Builder {
        private var resId: Int = -1
        private var menuId: Int = -1
        private var title: Int = -1
        private var menuItems: MutableList<Int> = mutableListOf()
        private var menuClicks: MutableList<MenuItem.OnMenuItemClickListener?> = mutableListOf()

        fun withId(@IdRes resId: Int) = apply { this.resId = resId }

        fun withTitle(title: Int) = apply { this.title = title }

        fun withMenu(@MenuRes menuId: Int) = apply { this.menuId = menuId}

        fun withMenuItems(menuItems: MutableList<Int>, menuClicks: MutableList<MenuItem.OnMenuItemClickListener?>) = apply {
            this.menuItems.addAll(menuItems)
            this.menuClicks.addAll(menuClicks)
        }

        fun build() = ToolbarFragment(resId, title, menuId, menuItems, menuClicks )
    }
}