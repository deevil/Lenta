package com.lenta.bp10.features.exit
import com.lenta.bp10.R
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class ExitWithConfirmationFragment : CoreMessageFragment() {


    override fun getPageNumber(): String = "10/93"

    override fun getViewModel(): MessageViewModel {
        provideViewModel(ExitFromAppViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.message = getString(R.string.exit_confirmation)
            vm.codeConfirm = 1
            return vm
        }
    }



    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        super.setupBottomToolBar(bottomToolbarUiModel)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }


}