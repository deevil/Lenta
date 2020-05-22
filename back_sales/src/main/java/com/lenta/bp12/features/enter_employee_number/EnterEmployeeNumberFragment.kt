package com.lenta.bp12.features.enter_employee_number

import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentEnterEmployeeNumberBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class EnterEmployeeNumberFragment : CoreFragment<FragmentEnterEmployeeNumberBinding, EnterEmployeeNumberViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_enter_employee_number

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("4")

    override fun getViewModel(): EnterEmployeeNumberViewModel {
        provideViewModel(EnterEmployeeNumberViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.definition_performer)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back, enabled = false)

        vm.fullName.observe(viewLifecycleOwner, Observer {
            bottomToolbarUiModel.uiModelButton5.requestFocus()
        })
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

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

}
