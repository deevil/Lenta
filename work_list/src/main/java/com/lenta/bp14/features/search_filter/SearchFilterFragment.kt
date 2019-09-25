package com.lenta.bp14.features.search_filter

import androidx.lifecycle.Observer
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentSearchFilterBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class SearchFilterFragment : CoreFragment<FragmentSearchFilterBinding, SearchFilterViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_search_filter

    override fun getPageNumber(): String?  = generateScreenNumberFromPostfix("111")

    override fun getViewModel(): SearchFilterViewModel {
        provideViewModel(SearchFilterViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.product_information)

        viewLifecycleOwner.apply {
            vm.taskName.observe(this, Observer { name ->
                topToolbarUiModel.title.value = name
            })
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.find)
    }

}
