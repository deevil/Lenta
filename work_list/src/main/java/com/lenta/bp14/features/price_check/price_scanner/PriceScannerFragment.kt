package com.lenta.bp14.features.price_check.price_scanner

import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentPriceScannerBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class PriceScannerFragment : CoreFragment<FragmentPriceScannerBinding, PriceScannerViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_price_scanner

    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("127")
    }

    override fun getViewModel(): PriceScannerViewModel {
        provideViewModel(PriceScannerViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.scan_price_description)


    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }


}
