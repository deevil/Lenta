package com.lenta.bp9.features.loading.tasks

import android.os.Bundle
import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.features.loading.CoreLoadingFragment
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class LoadingShipmentPurposeTransportFragment : CoreLoadingFragment() {

    companion object {
        fun create(mode: String, transportationNumber: String): LoadingShipmentPurposeTransportFragment {
            LoadingShipmentPurposeTransportFragment().let {
                it.mode = mode
                it.transportationNumber = transportationNumber
                return it
            }
        }
    }

    private var mode: String = ""
    private var transportationNumber: String = ""

    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("98")
    }

    override fun getViewModel(): CoreLoadingViewModel {
        provideViewModel(LoadingShipmentPurposeTransportViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.mode = mode
            vm.transportationNumber = transportationNumber
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        (vm as? LoadingShipmentPurposeTransportViewModel)?.let {
            topToolbarUiModel.description.value = it.taskDescription
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.visibility.value = false
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