package com.lenta.bp14.features.report_result

import android.os.Bundle
import android.view.View
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentReportResultBinding
import com.lenta.bp14.databinding.ItemTileTaskReportBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.provideViewModel

class ReportResultFragment : CoreFragment<FragmentReportResultBinding, ReportResultViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_report_result

    override fun getPageNumber(): String = "14/25"

    override fun getViewModel(): ReportResultViewModel {
        provideViewModel(ReportResultViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${getString(R.string.tk)} - ${vm.getMarket()}"
        topToolbarUiModel.description.value = getString(R.string.data_saving)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }


    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onNextClick()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvConfig = DataBindingRecyclerViewConfig<ItemTileTaskReportBinding>(layoutId = R.layout.item_tile_task_report, itemId = BR.vm)
    }


}
