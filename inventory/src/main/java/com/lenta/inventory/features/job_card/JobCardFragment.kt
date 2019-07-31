package com.lenta.inventory.features.job_card

import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentJobCardBinding
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.getDescriptionStringRes
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class JobCardFragment : CoreFragment<FragmentJobCardBinding, JobCardViewModel>(), ToolbarButtonsClickListener, OnBackPresserListener {

    private var taskNumber by state("")

    override fun getLayoutId(): Int = R.layout.fragment_job_card

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): JobCardViewModel {
        provideViewModel(JobCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            setupViewModel(it)
            return it
        }
    }

    private fun setupViewModel(vm: JobCardViewModel) {
        vm.init(
                taskNumber = taskNumber,
                typesRecount = listOf(
                        RecountType.Simple,
                        RecountType.ParallelByStorePlaces,
                        RecountType.ParallelByPerNo
                ),
                converterTypeToString = { recountType -> getString(recountType.getDescriptionStringRes()) }
        )
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.job_card)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    companion object {
        fun create(taskNumber: String): JobCardFragment {
            return JobCardFragment().apply {
                this.taskNumber = taskNumber
            }
        }
    }


    override fun onResume() {
        super.onResume()
        setupViewModel(vm)

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onBackPressed(): Boolean {
        return vm.onBackPressed()
    }


}
