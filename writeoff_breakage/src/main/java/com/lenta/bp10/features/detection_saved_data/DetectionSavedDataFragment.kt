package com.lenta.bp10.features.detection_saved_data

import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class DetectionSavedDataFragment : CoreMessageFragment() {
    override fun getPageNumber(): String {
        return "10/91"
    }

    override fun getViewModel(): MessageViewModel {
        provideViewModel(DetectionSavedDataViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.message = getString(R.string.saved_data_detect_message)
            vm.iconRes = R.drawable.ic_question
            vm.codeConfirmForRight = 1
            return vm
        }
    }

    override fun onToolbarButtonClick(view: View) {
        (vm as DetectionSavedDataViewModel).let {
            when (view.id) {
                R.id.b_3 -> it.onClickDelete()
                R.id.b_5 -> it.onClickRightButton()
            }
        }

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.goOver)
    }



}
