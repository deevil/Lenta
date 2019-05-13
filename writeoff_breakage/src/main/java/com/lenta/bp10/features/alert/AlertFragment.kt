package com.lenta.bp10.features.alert

import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AlertFragment : CoreMessageFragment() {


    override fun getPageNumber(): String = "???"


    override fun getViewModel(): MessageViewModel {
        provideViewModel(MessageViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            message?.let {
                vm.message = it
                vm.iconRes = iconRes
                vm.codeConfirm = codeConfirm
            }

            return vm
        }
    }


    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.visibility.value = true
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        if (codeConfirm != null) {
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
        }

    }

    override fun cleanTopToolbar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.uiModelButton1.visibility.value = false
        topToolbarUiModel.uiModelButton2.visibility.value = false
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickApply()
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