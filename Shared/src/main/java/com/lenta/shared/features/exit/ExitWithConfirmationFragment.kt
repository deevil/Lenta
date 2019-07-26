package com.lenta.shared.features.exit
import com.lenta.shared.R
import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class ExitWithConfirmationFragment : CoreMessageFragment() {


    override fun getPageNumber(): String {
        return generateScreenNumber()
    }

    override fun getViewModel(): MessageViewModel {
        provideViewModel(ExitFromAppViewModel::class.java).let {
            coreComponent.inject(it)
            it.message = getString(R.string.exit_confirmation)
            it.codeConfirmForRight = 1
            return it
        }
    }



    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        super.setupBottomToolBar(bottomToolbarUiModel)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }


}