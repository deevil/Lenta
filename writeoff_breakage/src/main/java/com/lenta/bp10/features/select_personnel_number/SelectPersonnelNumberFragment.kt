package com.lenta.bp10.features.select_personnel_number

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentSelectPersonnelNumberBinding
import com.lenta.bp10.platform.extentions.getAppComponent
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

    companion object {
        fun create(codeConfirmation: Int): SelectPersonnelNumberFragment {
            SelectPersonnelNumberFragment().let {
                it.codeConfirmation = codeConfirmation
                return it
            }
        }

    }

    override fun getLayoutId(): Int = R.layout.fragment_select_personnel_number

    override fun getPageNumber(): String = "10/12"

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

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.connectLiveData(vm.nextButtonFocus, getBottomToolBarUIModel()!!.uiModelButton5.requestFocus)
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
}