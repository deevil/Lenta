package com.lenta.shared.features.exit
import com.lenta.shared.features.message.MessageViewModel

class ExitFromAppViewModel : MessageViewModel() {
    override fun onClickApply() {
        coreNavigator.finishApp()
    }

}