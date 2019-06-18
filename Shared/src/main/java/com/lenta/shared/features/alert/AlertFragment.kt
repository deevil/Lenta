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
            vm.textColor = textColor

            return vm
        }
    }


    companion object {
        fun create(
                message: String,
                iconRes: Int = 0,
                textColor: Int? = null,
                pageNumber: String? = null,
                codeConfirm: Int? = null,
                codeConfirmForLeft: Int? = null,
                leftButtonDecorationInfo: ButtonDecorationInfo? = null,
                rightButtonDecorationInfo: ButtonDecorationInfo? = null
        ): AlertFragment {
            return AlertFragment().apply {
                this.message = message
                this.iconRes = iconRes
                this.textColor = textColor
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