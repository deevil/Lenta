package com.lenta.bp9.features.goods_information.mercury

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentGoodsMercuryInfoBinding
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.models.core.BarcodeData
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.DateInputMask
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class GoodsMercuryInfoFragment : CoreFragment<FragmentGoodsMercuryInfoBinding, GoodsMercuryInfoViewModel>(),
        ToolbarButtonsClickListener,
        OnScanResultListener,
        OnBackPresserListener {

    companion object {
        fun newInstance(productInfo: TaskProductInfo, isDiscrepancy: Boolean, barcodeData: BarcodeData?): GoodsMercuryInfoFragment {
            GoodsMercuryInfoFragment().let {
                it.productInfo = productInfo
                it.isDiscrepancy = isDiscrepancy
                it.barcodeData = barcodeData
                return it
            }
        }
    }

    private var isDiscrepancy by state<Boolean?>(null)
    private var productInfo by state<TaskProductInfo?>(null)
    private var barcodeData by state<BarcodeData?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_goods_mercury_info

    override fun getPageNumber(): String = "09/107"

    override fun getViewModel(): GoodsMercuryInfoViewModel {
        provideViewModel(GoodsMercuryInfoViewModel::class.java).let { vm->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = this.productInfo
            vm.isDiscrepancy.value = this.isDiscrepancy
            vm.barcodeData.value = this.barcodeData
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)
        topToolbarUiModel.title.value = "${vm.productInfo.value!!.getMaterialLastSix()} ${vm.productInfo.value!!.description}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.spinnerQuality?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionSpinQuality(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        binding?.spinnerManufacturers?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionSpinManufacturers(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        binding?.spinnerProductionDate?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionSpinProductionDate(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        binding?.etCount?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (vm.enabledApplyButton.value == true) {
                    vm.onClickApply()
                }
                return@OnKeyListener true
            }
            false
        })

        DateInputMask(binding?.etProductionDate!!).listen()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickAdd()
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
