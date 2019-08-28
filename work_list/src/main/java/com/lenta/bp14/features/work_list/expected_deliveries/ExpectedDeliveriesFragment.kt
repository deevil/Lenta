package com.lenta.bp14.features.work_list.expected_deliveries

import android.os.Bundle
import android.view.View
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentExpectedDeliveriesBinding
import com.lenta.bp14.databinding.ItemTileDeliveriesBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.provideViewModel

class ExpectedDeliveriesFragment : CoreFragment<FragmentExpectedDeliveriesBinding, ExpectedDeliveriesViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_expected_deliveries

    override fun getPageNumber(): String = "14/21"

    override fun getViewModel(): ExpectedDeliveriesViewModel {
        provideViewModel(ExpectedDeliveriesViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.expected_deliveries)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.update)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvConfig = DataBindingRecyclerViewConfig<ItemTileDeliveriesBinding>(
                layoutId = R.layout.item_tile_deliveries,
                itemId = BR.vm
        )
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickRefresh()
        }
    }




}