package com.lenta.shared.features.test_environment.failure

import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import javax.inject.Inject

class FailurePinCodeFragment : CoreMessageFragment() {

    private lateinit var failure: Failure

    @Inject
    lateinit var failureInterpreter: IFailureInterpreter

    companion object {
        fun create(failure: Failure): FailurePinCodeFragment {
            FailurePinCodeFragment().let {
                it.failure = failure
                return it
            }
        }

    }

    override fun getViewModel(): MessageViewModel {
        provideViewModel(FailurePinCodeViewModel::class.java).let { vm ->
            coreComponent.inject(vm)
            vm.message = failureInterpreter.getFailureDescription(failure)
            vm.codeConfirm = null
            return vm
        }
    }

    override fun getPageNumber(): String {
        return "10/96"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.visibility.value = false
    }
}