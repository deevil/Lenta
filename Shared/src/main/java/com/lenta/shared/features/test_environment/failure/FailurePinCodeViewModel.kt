package com.lenta.shared.features.test_environment.failure

import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.features.message.usecase.GoBackParams
import com.lenta.shared.utilities.extentions.launchUITryCatch

class FailurePinCodeViewModel : MessageViewModel() {

    init {
        launchUITryCatch {
            goBackWithDelay(GoBackParams(timeInMillis = 3000L))
        }
    }
}