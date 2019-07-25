package com.lenta.shared.features.test_environment.failure

import androidx.lifecycle.viewModelScope
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.features.message.usecase.GoBackParams
import kotlinx.coroutines.launch

class FailurePinCodeViewModel : MessageViewModel() {

    init {
        viewModelScope.launch {
            goBackWithDelay(GoBackParams(timeInMillis = 3000L))
        }
    }
}