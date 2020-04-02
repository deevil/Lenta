package com.lenta.bp9.features.repres_person_num_entry

import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentRepresPersonNumEntryBinding
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class RepresPersonNumEntryFragment : CoreFragment<FragmentRepresPersonNumEntryBinding, RepresPersonNumEntryViewModel>(),
        ToolbarButtonsClickListener {

    companion object {
        fun create(sectionInfo: TaskSectionInfo): RepresPersonNumEntryFragment {
            RepresPersonNumEntryFragment().let {
                it.sectionInfo = sectionInfo
                return it
            }
        }
    }

    private var sectionInfo by state<TaskSectionInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_repres_person_num_entry

    override fun getPageNumber(): String = "09/71"

    override fun getViewModel(): RepresPersonNumEntryViewModel {
        provideViewModel(RepresPersonNumEntryViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.sectionInfo.value = sectionInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.section) + " " + vm.getDescription()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

        connectLiveData(vm.enabledBtnNext, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

}
