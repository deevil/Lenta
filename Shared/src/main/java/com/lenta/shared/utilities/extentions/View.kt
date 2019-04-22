package com.lenta.shared.utilities.extentions

import android.view.View


fun View.setVisible(visibility: Boolean = true) {
    this.visibility = if (visibility) View.VISIBLE else View.GONE
}

fun View.setInvisible(invisible: Boolean = true) {
    visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}