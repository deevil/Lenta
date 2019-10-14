package com.lenta.bp14.features.print_settings

import android.view.View
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentPrintSettingsBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.provideViewModel

class PrintSettingsFragment : CoreFragment<FragmentPrintSettingsBinding, PrintSettingsViewModel>(), OnScanResultListener, ToolbarButtonsClickListener {


    override fun getLayoutId(): Int = R.layout.fragment_print_settings

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("7")

    override fun getViewModel(): PrintSettingsViewModel {
        provideViewModel(PrintSettingsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        connectLiveData(
                vm.title.map {
                    if (it.isNullOrBlank()) getString(R.string.specify_product) else it
                }, topToolbarUiModel.title)
        topToolbarUiModel.description.value = getString(R.string.print_settings)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.print)
        connectLiveData(vm.isPrintEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickPrint()
        }


    }

}
