package com.lenta.bp14.features.job_card

import android.view.View
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentJobCardBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class JobCardFragment : CoreFragment<FragmentJobCardBinding, JobCardViewModel>(), ToolbarButtonsClickListener {

    var taskNumber: String by state("")

    override fun getLayoutId(): Int = R.layout.fragment_job_card

    override fun getPageNumber(): String = "14/??"

    override fun getViewModel(): JobCardViewModel {
        provideViewModel(JobCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.setTaskNumber(taskNumber)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${getString(R.string.tk)} - ${vm.getMarket()}"
        topToolbarUiModel.description.value = getString(R.string.job_card)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }


    companion object {
        fun create(taskNumber: String): JobCardFragment {
            return JobCardFragment().apply {
                this.taskNumber = taskNumber
            }

        }
    }

}