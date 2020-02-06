package com.lenta.shared.features.test_environment.failure

import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class FailurePinCodeFragment : CoreMessageFragment() {

    companion object {
        fun create(message: String): FailurePinCodeFragment {
            FailurePinCodeFragment().let {
                it.message = message
                return it
            }
        }

    }

    override fun getViewModel(): MessageViewModel {
        provideViewModel(FailurePinCodeViewModel::class.java).let { vm ->
            coreComponent.inject(vm)
            vm.message = message
            vm.codeConfirmForRight = null
            return vm
        }
    }

    override fun getPageNumber(): String {
        return generateScreenNumber()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.hide()
    }
}