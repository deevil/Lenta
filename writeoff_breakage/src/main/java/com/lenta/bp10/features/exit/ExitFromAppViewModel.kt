package com.lenta.bp10.features.exit
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.message.MessageViewModel
import javax.inject.Inject

class ExitFromAppViewModel : MessageViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator


    override fun onClickApply() {
        screenNavigator.finishApp()

    }

}