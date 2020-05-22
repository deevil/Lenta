package com.lenta.inventory.features.select_personnel_number

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.inventory.R
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class SelectPersonnelNumberFragment : CoreFragment<com.lenta.inventory.databinding.FragmentSelectPersonnelNumberBinding, SelectPersonnelNumberViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener, OnBackPresserListener {

    companion object {
        fun create(isScreenMainMenu: Boolean): SelectPersonnelNumberFragment {
            SelectPersonnelNumberFragment().let {
                it.isScreenMainMenu = isScreenMainMenu
                return it
            }
        }
    }

    private var isScreenMainMenu by state<Boolean?>(null)
    override fun getLayoutId(): Int = R.layout.fragment_select_personnel_number

    override fun getPageNumber(): String = "11/12"

    override fun getViewModel(): SelectPersonnelNumberViewModel {
        provideViewModel(SelectPersonnelNumberViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.isScreenMainMenu.value = this.isScreenMainMenu
            return vm
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
        connectLiveData(vm.enabledBackButton, bottomToolbarUiModel.uiModelButton1.enabled)
        connectLiveData(vm.enabledNextButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }


    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickNext()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }
}