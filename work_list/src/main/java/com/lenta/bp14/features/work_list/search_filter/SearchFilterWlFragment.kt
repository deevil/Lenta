package com.lenta.bp14.features.work_list.search_filter

import androidx.lifecycle.Observer
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentSearchFilterWlBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class SearchFilterWlFragment : CoreFragment<FragmentSearchFilterWlBinding, SearchFilterWlViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_search_filter_wl

    override fun getPageNumber(): String?  = generateScreenNumberFromPostfix("111")

    override fun getViewModel(): SearchFilterWlViewModel {
        provideViewModel(SearchFilterWlViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.product_information)

        vm.taskName.observe(this, Observer<String> { name ->
            topToolbarUiModel.title.value = name
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.toFind)
    }

}