package com.lenta.bp9.features.goods_information.marking.uom_st_without_counting_in_boxes

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentMarkingInfoBinding
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
import com.lenta.shared.utilities.state.state

//https://trello.com/c/NGsFfWgB
class MarkingInfoFragment : CoreFragment<FragmentMarkingInfoBinding, MarkingInfoViewModel>(),
        ToolbarButtonsClickListener,
        OnScanResultListener,
        OnBackPresserListener {

    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_marking_info

    override fun getPageNumber(): String = PAGE_NUMBER

    override fun getViewModel(): MarkingInfoViewModel {
        provideViewModel(MarkingInfoViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            this.productInfo?.let {
                vm.productInfo.value = it
                vm.processMarkingProductService.initProduct(it)
            }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value?.getMaterialLastSix()} ${vm.productInfo.value?.description}"
        topToolbarUiModel.description.value = getString(R.string.goods_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledRollbackBtn, bottomToolbarUiModel.uiModelButton2.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.spinnerQuality?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionSpinQuality(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) = Unit
        }

        binding?.etCount?.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
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
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onResume() {
        super.onResume()
        vm.requestFocusToCount.value = true
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        private const val PAGE_NUMBER = "09/44"
        fun newInstance(productInfo: TaskProductInfo): MarkingInfoFragment {
            MarkingInfoFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

}
