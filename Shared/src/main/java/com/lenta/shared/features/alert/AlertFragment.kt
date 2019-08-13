package com.lenta.shared.features.alert


import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AlertFragment : CoreMessageFragment() {

    override fun getViewModel(): MessageViewModel {
        provideViewModel(MessageViewModel::class.java).let { vm ->
            coreComponent.inject(vm)
            vm.message = message
            vm.iconRes = iconRes
            vm.codeConfirmForExit = codeConfirmForExit
            vm.codeConfirmForRight = codeConfirmForRight
            vm.codeConfirmForButton2 = codeConfirmForButton2
            vm.codeConfirmForButton3 = codeConfirmForButton3
            vm.codeConfirmForButton4 = codeConfirmForButton4
            vm.codeConfirmForLeft = codeConfirmForLeft
            vm.textColor = textColor
            vm.timeAutoExitInMillis = timeAutoExitInMillis
            return vm
        }
    }

    override fun cleanTopToolbar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.uiModelButton1.visibility.value = false
        topToolbarUiModel.uiModelButton2.visibility.value = false
    }

    companion object {
        fun create(
                message: String,
                title: String? = null,
                description: String? = null,
                iconRes: Int = 0,
                textColor: Int? = null,
                pageNumber: String? = null,
                codeConfirmForRight: Int? = null,
                codeConfirmForButton2: Int? = null,
                codeConfirmForButton3: Int? = null,
                codeConfirmForButton4: Int? = null,
                codeConfirmForLeft: Int? = null,
                codeConfirmForExit: Int? = null,
                isVisibleLeftButton: Boolean = true,
                isForceVisibleRigthButton: Boolean = false,
                leftButtonDecorationInfo: ButtonDecorationInfo? = null,
                buttonDecorationInfo2: ButtonDecorationInfo? = null,
                buttonDecorationInfo3: ButtonDecorationInfo? = null,
                buttonDecorationInfo4: ButtonDecorationInfo? = null,
                rightButtonDecorationInfo: ButtonDecorationInfo? = null,
                timeAutoExitInMillis: Int? = null
        ): AlertFragment {
            return AlertFragment().apply {
                this.message = message
                this.title = title
                this.description = description
                this.iconRes = iconRes
                this.textColor = textColor
                this.codeConfirmForExit = codeConfirmForExit
                this.codeConfirmForRight = codeConfirmForRight
                this.codeConfirmForButton2 = codeConfirmForButton2
                this.codeConfirmForButton3 = codeConfirmForButton3
                this.codeConfirmForButton4 = codeConfirmForButton4
                this.codeConfirmForLeft = codeConfirmForLeft
                this.isVisibleLeftButton = isVisibleLeftButton
                this.isForceVisibleRigthButton = isForceVisibleRigthButton
                this.pageNumb = pageNumber
                this.timeAutoExitInMillis = timeAutoExitInMillis

                leftButtonDecorationInfo?.let {
                    this.leftButtonDecorationInfo = it
                }
                buttonDecorationInfo2?.let {
                    this.buttonDecorationInfo2 = it
                }
                buttonDecorationInfo3?.let {
                    this.buttonDecorationInfo3 = it
                }
                buttonDecorationInfo4?.let {
                    this.buttonDecorationInfo4 = it
                }
                rightButtonDecorationInfo?.let {
                    this.rightButtonDecorationInfo = it
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return vm.onBackPressed()
    }

}