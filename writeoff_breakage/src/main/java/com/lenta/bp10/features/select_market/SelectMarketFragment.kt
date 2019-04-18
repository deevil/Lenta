package com.lenta.bp10.features.select_market

import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentSelectMarketBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectMarketFragment : CoreFragment<FragmentSelectMarketBinding, SelectMarketViewModel>() {
    override fun getLayoutId(): Int = R.layout.fragment_select_market


    override fun getPageNumber(): String = "10/03"

    override fun getViewModel(): SelectMarketViewModel {
        provideViewModel(SelectMarketViewModel::class.java).let {
            getAppComponent()?.inject(this)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.tk_selection)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel
                .uiModelButton5.show(ButtonDecorationInfo.next)
    }
}