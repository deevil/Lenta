package com.lenta.shared.features.test_environment.failure

import androidx.lifecycle.viewModelScope
import com.lenta.shared.features.message.MessageViewModel
import kotlinx.coroutines.launch

class FailurePinCodeViewModel : MessageViewModel() {

    init {
        viewModelScope.launch {
            if (codeConfirm == null) {
                goBackWithDelay(params = null)
            }

        }
    }
}