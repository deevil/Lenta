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
            vm.textColor = textColor

            return vm
        }
    }


    companion object {
        fun create(
                message: String,
                iconRes: Int = 0,
                textColor: Int? = null,
                pageNumber: String = "???",
                codeConfirm: Int? = null,
                leftButtonDecorationInfo: ButtonDecorationInfo? = null,
                rightButtonDecorationInfo: ButtonDecorationInfo? = null
        ): AlertFragment {
            AlertFragment().let { alertFragment ->
                alertFragment.message = message
                alertFragment.iconRes = iconRes
                alertFragment.textColor = textColor
                alertFragment.codeConfirm = codeConfirm
                alertFragment.pageNumb = pageNumber
                leftButtonDecorationInfo?.let {
                    alertFragment.leftButtonDecorationInfo = it
                }
                rightButtonDecorationInfo?.let {
                    alertFragment.rightButtonDecorationInfo = it
                }

                return alertFragment
            }
        }
    }

}