package com.lenta.shared.features.select_oper_mode

import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentSelectOperModeBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectOperModeFragment : CoreFragment<FragmentSelectOperModeBinding, SelectOperModeViewModel>() {
    override fun getLayoutId(): Int = R.layout.fragment_select_oper_mode

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.select_oper_mode)
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.back)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll(false)
    }

    override fun getPageNumber(): String = "10/54"

    override fun getViewModel(): SelectOperModeViewModel {
        provideViewModel(SelectOperModeViewModel::class.java).let {
            coreComponent.inject(it)
            return it
        }
    }
}