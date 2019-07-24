package com.lenta.inventory.features.select_personnel_number

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.inventory.R
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectPersonnelNumberFragment : CoreFragment<com.lenta.inventory.databinding.FragmentSelectPersonnelNumberBinding, SelectPersonnelNumberViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener {
    override fun getLayoutId(): Int = R.layout.fragment_select_personnel_number

    override fun getPageNumber(): String = "11/12"

    override fun getViewModel(): SelectPersonnelNumberViewModel {
        provideViewModel(SelectPersonnelNumberViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.definition_performer)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel
                .uiModelButton5.show(ButtonDecorationInfo.next)
        bottomToolbarUiModel
                .uiModelButton1.show(ButtonDecorationInfo.back, enabled = false)
        vm.fullName.observe(viewLifecycleOwner, Observer {
            bottomToolbarUiModel
                    .uiModelButton5.requestFocus()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBottomToolBarUIModel()?.let {
            connectLiveData(vm.enabledNextButton, it.uiModelButton5.enabled)
        }

    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickNext()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }
}