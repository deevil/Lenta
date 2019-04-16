package com.lenta.shared.features.message

import androidx.lifecycle.viewModelScope
import com.lenta.shared.features.message.usecase.DelayGoBack
import com.lenta.shared.platform.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageViewModel : BaseViewModel() {
    @Inject
    lateinit var goBackWithDelay: DelayGoBack

    lateinit var message: String

    init {
        viewModelScope.launch {
            goBackWithDelay(params = null)
        }
    }

}