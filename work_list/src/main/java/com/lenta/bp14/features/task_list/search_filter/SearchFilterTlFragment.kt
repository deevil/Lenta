package com.lenta.bp14.features.task_list.search_filter

import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentSearchFilterTlBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SearchFilterTlFragment : CoreFragment<FragmentSearchFilterTlBinding, SearchFilterTlViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_search_filter_tl

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): SearchFilterTlViewModel {
        provideViewModel(SearchFilterTlViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
