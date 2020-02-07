package com.lenta.bp9.features.loading.tasks

import android.os.Bundle
import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.features.loading.CoreLoadingFragment
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class LoadingRegisterArrivalFragment : CoreLoadingFragment() {

    companion object {
        fun create(isInStockPaperTTN: Boolean, isEdo: Boolean, status: TaskStatus): LoadingRegisterArrivalFragment {
            LoadingRegisterArrivalFragment().let {
                it.isInStockPaperTTN = isInStockPaperTTN
                it.isEdo = isEdo
                it.status = status
                return it
            }
        }
    }

    private var isInStockPaperTTN by state<Boolean?>(false)
    private var isEdo by state<Boolean?>(false)
    private var status by state<TaskStatus?>(TaskStatus.Other)

    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("98")
    }

    override fun getViewModel(): CoreLoadingViewModel {
        provideViewModel(LoadingRegisterArrivalViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.isInStockPaperTTN.value = this.isInStockPaperTTN
            vm.isEdo.value = this.isEdo
            vm.status.value = this.status
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        (vm as? LoadingRegisterArrivalViewModel)?.let {
            topToolbarUiModel.description.value = it.taskDescription
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.hide()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onToolbarButtonClick(view: View) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.title.value = getString(R.string.status_change)
    }
}