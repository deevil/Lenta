package com.lenta.bp14.features.task_list.search_filter

import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentSearchFilterTlBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class SearchFilterTlFragment : CoreFragment<FragmentSearchFilterTlBinding, SearchFilterTlViewModel>(),
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_search_filter_tl

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("121")

    override fun getViewModel(): SearchFilterTlViewModel {
        provideViewModel(SearchFilterTlViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.task_list)

        vm.marketNumber.observe(this, Observer<String> { marketNumber ->
            topToolbarUiModel.title.value = getString(R.string.title_market_number, marketNumber)
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.find)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickFind()
        }
    }


}
