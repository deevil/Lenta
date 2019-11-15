package com.lenta.bp16.features.raw_good_list

import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodListBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class RawGoodListFragment : CoreFragment<FragmentGoodListBinding, RawGoodListViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_good_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("6")

    override fun getViewModel(): RawGoodListViewModel {
        provideViewModel(RawGoodListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_list)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

}
