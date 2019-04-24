package com.lenta.bp10.features.alert

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.message.CoreMessageFragment
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AlertFragment : CoreMessageFragment() {

    override fun getPageNumber(): String = "???"


    override fun getViewModel(): MessageViewModel {
        provideViewModel(MessageViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            message?.let {
                vm.message = it
            }

            return vm
        }
    }

    companion object {
        fun create(message: String): AlertFragment {
            AlertFragment().let {
                it.message = message
                return it
            }
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.visibility.value = true
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.visibility.value = false
    }

    override fun cleanTopToolbar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.uiModelButton1.visibility.value = false
        topToolbarUiModel.uiModelButton2.visibility.value = false
    }

}