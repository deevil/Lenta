package com.lenta.shared.features.alert


import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class AlertFragment : CoreMessageFragment() {

    override fun getViewModel(): MessageViewModel {
        provideViewModel(MessageViewModel::class.java).let { vm ->
            coreComponent.inject(vm)
            vm.message = message
            vm.iconRes = iconRes
            vm.codeConfirm = codeConfirm
            vm.codeConfirmForLeft = codeConfirmForLeft
            return vm
        }
    }


    companion object {
        fun create(
                message: String,
                iconRes: Int = 0,
                pageNumber: String = "???",
                codeConfirm: Int? = null,
                codeConfirmForLeft: Int? = null,
                leftButtonDecorationInfo: ButtonDecorationInfo? = null,
                rightButtonDecorationInfo: ButtonDecorationInfo? = null
        ): AlertFragment {
            return AlertFragment().apply {
                this.message = message
                this.iconRes = iconRes
                this.codeConfirm = codeConfirm
                this.codeConfirmForLeft = codeConfirmForLeft
                this.pageNumb = pageNumber
                leftButtonDecorationInfo?.let {
                    this.leftButtonDecorationInfo = it
                }
                rightButtonDecorationInfo?.let {
                    this.rightButtonDecorationInfo = it
                }


            }
        }
    }

}