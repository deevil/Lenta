package com.lenta.shared.features.alert


import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AlertFragment : CoreMessageFragment() {


    override fun getPageNumber(): String = "???"


    override fun getViewModel(): MessageViewModel {
        provideViewModel(MessageViewModel::class.java).let { vm ->
            coreComponent.inject(vm)
            vm.message = message
            vm.iconRes = iconRes
            vm.codeConfirm = codeConfirm

            return vm
        }
    }




    companion object {
        fun create(
                message: String,
                iconRes: Int = 0,
                codeConfirm: Int? = null): AlertFragment {
            AlertFragment().let {
                it.message = message
                it.iconRes = iconRes
                it.codeConfirm = codeConfirm
                return it
            }
        }
    }

}