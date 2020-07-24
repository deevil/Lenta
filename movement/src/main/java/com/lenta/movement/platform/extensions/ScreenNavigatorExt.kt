package com.lenta.movement.platform.extensions

import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure

fun IScreenNavigator.openAlertScreenWithFailure(failure: Failure) {
    hideProgress()
    openAlertScreen(failure)
}