package com.lenta.bp9.features.goods_information.general.task_opp

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentGoodsInfoShipmentPpBinding
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.state.state

class GoodsInfoShipmentPPFragment : CoreFragment<FragmentGoodsInfoShipmentPpBinding, GoodsInfoShipmentPPViewModel>(),
        ToolbarButtonsClickListener,
        OnScanResultListener,
        OnBackPresserListener {

    companion object {
        fun create(productInfo: TaskProductInfo, isDiscrepancy: Boolean, initialCount: Double = 0.0): GoodsInfoShipmentPPFragment {
            GoodsInfoShipmentPPFragment().let {
                it.productInfo = productInfo
                it.isDiscrepancy = isDiscrepancy
                it.initialCount = initialCount
                return it
            }
        }
    }

    private var isDiscrepancy by state<Boolean?>(null)
    private var productInfo by state<TaskProductInfo?>(null)
    private var initialCount by state<Double>(0.0)

    override fun getLayoutId(): Int = R.layout.fragment_goods_info_shipment_pp

    override fun getPageNumber(): String = "09/113"

    override fun getViewModel(): GoodsInfoShipmentPPViewModel {
        provideViewModel(GoodsInfoShipmentPPViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = this.productInfo
            vm.isDiscrepancy.value = this.isDiscrepancy
            vm.count.value = initialCount.toStringFormatted()
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value!!.getMaterialLastSix()} ${vm.productInfo.value!!.description}"
        topToolbarUiModel.description.value = getString(R.string.goods_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledMissingButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.etQuantity?.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (vm.enabledApplyButton.value == true) {
                    vm.onClickApply()
                }
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onResume() {
        super.onResume()
        vm.requestFocusToCount.value = true
    }
}
