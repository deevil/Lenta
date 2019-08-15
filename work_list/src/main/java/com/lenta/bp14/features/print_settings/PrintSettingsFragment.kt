package com.lenta.bp14.features.print_settings

import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentPrintSettingsBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class PrintSettingsFragment : CoreFragment<FragmentPrintSettingsBinding, PrintSettingsViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_print_settings

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("7")

    override fun getViewModel(): PrintSettingsViewModel {
        provideViewModel(PrintSettingsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = getString(R.string.specify_product)
        topToolbarUiModel.description.value = getString(R.string.print_settings)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.print)
    }

}
