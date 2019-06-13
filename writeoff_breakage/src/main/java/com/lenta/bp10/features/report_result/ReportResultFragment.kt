package com.lenta.bp10.features.report_result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenta.bp10.BR
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentReportResultBinding
import com.lenta.bp10.databinding.ItemTaskReportBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.bp10.requests.network.WriteOffReportResponse
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class ReportResultFragment : CoreFragment<FragmentReportResultBinding, ReportResultViewModel>(),
        OnBackPresserListener, ToolbarButtonsClickListener {

    var writeOffReportResponse by state<WriteOffReportResponse?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_report_result

    override fun getPageNumber() = "10/09"

    override fun getViewModel(): ReportResultViewModel {
        provideViewModel(ReportResultViewModel::class.java).let { viewModel ->
            getAppComponent()?.inject(viewModel)
            writeOffReportResponse?.let {
                viewModel.setWriteOffReportResponse(it)
            }
            return viewModel
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding?.rvConfig = DataBindingRecyclerViewConfig<ItemTaskReportBinding>(layoutId = R.layout.item_task_report, itemId = BR.vm)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${getString(R.string.tk)} - ${vm.getMarket()}"
        topToolbarUiModel.description.value = getString(R.string.data_saving)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onNextClick()
        }
    }

    companion object {
        fun create(writeOffReportResponse: WriteOffReportResponse): ReportResultFragment {
            return ReportResultFragment().apply {
                this.writeOffReportResponse = writeOffReportResponse
            }
        }
    }


}
