package com.lenta.bp14.features.barcode_detection

import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentCoreScanBarCodeBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class CoreScanBarCodeFragment : CoreFragment<FragmentCoreScanBarCodeBinding, CoreScanBarCodeViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_core_scan_bar_code

    override fun getPageNumber(): String {
        return "14/SC"
    }

    override fun getViewModel(): CoreScanBarCodeViewModel {
        provideViewModel(CoreScanBarCodeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }


}
