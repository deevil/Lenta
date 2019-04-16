package com.lenta.bp10.features.alert

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AlertFragment : CoreMessageFragment() {

    override fun getViewModel(): MessageViewModel {
        provideViewModel(MessageViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            message?.let {
                vm.message = it
            }

            return vm
        }
    }

    companion object {
        fun create(message: String): AlertFragment {
            AlertFragment().let {
                it.message = message
                return it
            }
        }
    }

}