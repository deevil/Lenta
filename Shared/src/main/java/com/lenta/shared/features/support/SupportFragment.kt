package com.lenta.shared.features.support

import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentSupportBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class SupportFragment : CoreFragment<FragmentSupportBinding, SupportViewModel>() {
    override fun getLayoutId(): Int = R.layout.fragment_support


    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.support)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): SupportViewModel {
        provideViewModel(SupportViewModel::class.java).let {
            coreComponent.inject(it)
            return it
        }
    }
}