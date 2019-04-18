package com.lenta.shared.platform.battery_state

import com.lenta.shared.R


fun getIconForStatus(isCharging: Boolean?, level: Int?): Int {
    if (isCharging == null || level == null) {
        return R.drawable.ic_battery_unknown_white_16dp
    }
    if (isCharging == true) {
        return when {
            level > 99 -> R.drawable.ic_battery_charging_full_white_16dp
            level > 89 -> R.drawable.ic_battery_charging_90_white_16dp
            level > 79 -> R.drawable.ic_battery_charging_80_white_16dp
            level > 59 -> R.drawable.ic_battery_charging_60_white_16dp
            level > 49 -> R.drawable.ic_battery_charging_50_white_16dp
            level > 29 -> R.drawable.ic_battery_charging_30_white_16dp
            else -> R.drawable.ic_battery_charging_20_white_16dp
        }
    }

    return when {
        level > 99 -> R.drawable.ic_battery_full_white_16dp
        level > 89 -> R.drawable.ic_battery_90_white_16dp
        level > 79 -> R.drawable.ic_battery_80_white_16dp
        level > 59 -> R.drawable.ic_battery_60_white_16dp
        level > 49 -> R.drawable.ic_battery_50_white_16dp
        level > 29 -> R.drawable.ic_battery_30_white_16dp
        else -> R.drawable.ic_battery_20_white_16dp
    }

}


