package com.CP.Savour

import android.support.annotation.IdRes
import android.support.annotation.MenuRes
import android.support.annotation.StringRes
import android.view.MenuItem

class FragmentToolbar(@IdRes val resId: Int,
                      @StringRes title: String,
                      @MenuRes menuId: Int,
                      val menuItems: MutableList<Int>,
                      val menuClicks: MutableList<MenuItem.OnMenuItemClickListener>) {

    companion object {
        @JvmField val NO_TOOLBAR = -1
    }

    class Builder {
        private var resId: Int = -1
        private var menuId: Int = -1
        private var title: Int = -1
    }
}