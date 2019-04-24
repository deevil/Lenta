package com.lenta.bp10.features.settings

import com.lenta.bp10.R
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SettingsFragment : CoreFragment<com.lenta.bp10.databinding.FragmentSettingsBinding, SettingsViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_settings

    override fun getPageNumber() = "10/02"

    override fun getViewModel(): SettingsViewModel {
        provideViewModel(SettingsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = "Dsdsda"
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.home)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.visibility.value = false
    }
}