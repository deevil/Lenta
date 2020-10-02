package com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed.box_card

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentExciseAlcoBoxCardPgeBinding
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskExciseStampInfo
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

class ExciseAlcoBoxCardPGEFragment : CoreFragment<FragmentExciseAlcoBoxCardPgeBinding, ExciseAlcoBoxCardPGEViewModel>(),
        OnScanResultListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    private var productInfo by state<TaskProductInfo?>(null)
    private var boxInfo by state<TaskBoxInfo?>(null)
    private var massProcessingBoxesNumber by state<List<String>?>(null)
    private var exciseStampInfo by state<TaskExciseStampInfo?>(null)
    private var selectQualityCode by state<String?>(null)
    private var isScan by state<Boolean?>(null)
    private var isBoxNotIncludedInNetworkLenta by state<Boolean?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_excise_alco_box_card_pge

    override fun getPageNumber(): String = PAGE_NUMBER

    override fun getViewModel(): ExciseAlcoBoxCardPGEViewModel {
        provideViewModel(ExciseAlcoBoxCardPGEViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = this.productInfo
            vm.selectQualityCode.value = this.selectQualityCode
            vm.isScan.value = this.isScan
            vm.isBoxNotIncludedInNetworkLenta.value = this.isBoxNotIncludedInNetworkLenta
            boxInfo?.let {
                vm.boxInfo.value = it
            }
            massProcessingBoxesNumber?.let {
                vm.massProcessingBoxesNumber.value = it
            }
            exciseStampInfo?.let {
                vm.exciseStampInfo.value = it
            }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value?.getMaterialLastSix()} ${vm.productInfo.value?.description}"
        topToolbarUiModel.description.value = vm.getDescription()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledRollbackBtn, bottomToolbarUiModel.uiModelButton2.enabled)
        connectLiveData(vm.enabledDetailsBtn, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.enabledAddBtn, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.etCount?.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (vm.enabledApplyBtn.value == true) {
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

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    companion object {
        private const val PAGE_NUMBER = "09/62"

        fun create(
                productInfo: TaskProductInfo,
                boxInfo: TaskBoxInfo?,
                massProcessingBoxesNumber: List<String>?,
                exciseStampInfo: TaskExciseStampInfo?,
                selectQualityCode: String,
                isScan: Boolean,
                isBoxNotIncludedInNetworkLenta: Boolean): ExciseAlcoBoxCardPGEFragment {
            ExciseAlcoBoxCardPGEFragment().let {
                it.productInfo = productInfo
                it.boxInfo = boxInfo
                it.massProcessingBoxesNumber = massProcessingBoxesNumber
                it.exciseStampInfo = exciseStampInfo
                it.selectQualityCode = selectQualityCode
                it.isScan = isScan
                it.isBoxNotIncludedInNetworkLenta = isBoxNotIncludedInNetworkLenta
                return it
            }
        }
    }

}
