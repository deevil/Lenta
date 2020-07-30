package com.lenta.movement.features.selectpersonalnumber

import android.view.View
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentSelectPersonnelNumberBinding
import com.lenta.movement.platform.extensions.getAppComponent
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

class SelectPersonnelNumberFragment : CoreFragment<FragmentSelectPersonnelNumberBinding, SelectPersonnelNumberViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener {

    private var codeConfirmation: Int? by state<Int?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_select_personnel_number

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): SelectPersonnelNumberViewModel {
        provideViewModel(SelectPersonnelNumberViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.setCodeConfirm(codeConfirmation)
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
                .uiModelButton1.show(ButtonDecorationInfo.back, enabled = codeConfirmation != null)

        connectLiveData(vm.nextButtonFocus, bottomToolbarUiModel.uiModelButton5.requestFocus)
        connectLiveData(vm.nextButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)

    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickNext()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    companion object {
        private const val PAGE_NUMBER = "13/12"

        fun create(codeConfirmation: Int): SelectPersonnelNumberFragment {
            SelectPersonnelNumberFragment().let {
                it.codeConfirmation = codeConfirmation
                return it
            }
        }
    }
}