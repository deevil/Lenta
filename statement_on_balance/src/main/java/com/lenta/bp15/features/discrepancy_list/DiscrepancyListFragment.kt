package com.lenta.bp15.features.discrepancy_list

import android.os.Bundle
import android.view.View
import com.lenta.bp15.R
import com.lenta.bp15.BR
import com.lenta.bp15.databinding.FragmentDiscrepancyListBinding
import com.lenta.bp15.databinding.ItemDiscrepansyListBinding
import com.lenta.bp15.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class DiscrepancyListFragment : CoreFragment<FragmentDiscrepancyListBinding, DiscrepancyListViewModel>(),
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_discrepancy_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): DiscrepancyListViewModel {
        provideViewModel(DiscrepancyListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_list)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.skip)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickSkip()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDiscrepancyList()
    }

    private fun initDiscrepancyList() {
        binding?.rvConfig = DataBindingRecyclerViewConfig<ItemDiscrepansyListBinding>(
                layoutId = R.layout.item_discrepansy_list,
                itemId = BR.item
        )
    }

    companion object {
        const val SCREEN_NUMBER = "21"
    }

}