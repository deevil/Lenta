package com.lenta.bp7.features.select_check_type

import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentSelectCheckTypeBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectCheckTypeFragment : CoreFragment<FragmentSelectCheckTypeBinding, SelectCheckTypeViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_select_check_type

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("05")

    override fun getViewModel(): SelectCheckTypeViewModel {
        provideViewModel(SelectCheckTypeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.description_main_menu)
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.visibility.value = false
    }
}