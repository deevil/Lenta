package com.lenta.bp9.features.goods_information.excise_alco_pge.excise_alco_box_acc_pge.excise_alco_box_card

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

    companion object {
        fun create(
                productInfo: TaskProductInfo,
                boxInfo: TaskBoxInfo?,
                massProcessingBoxesNumber: List<String>?,
                exciseStampInfo: TaskExciseStampInfo?,
                selectQualityCode: String,
                initialCount: String,
                isScan: Boolean,
                countAcceptRefusal: Double): ExciseAlcoBoxCardPGEFragment {
            ExciseAlcoBoxCardPGEFragment().let {
                it.productInfo = productInfo
                it.boxInfo = boxInfo
                it.massProcessingBoxesNumber = massProcessingBoxesNumber
                it.exciseStampInfo = exciseStampInfo
                it.selectQualityCode = selectQualityCode
                it.initialCount = initialCount
                it.isScan = isScan
                it.countAcceptRefusal = countAcceptRefusal
                return it
            }
        }
    }

    private var productInfo by state<TaskProductInfo?>(null)
    private var boxInfo by state<TaskBoxInfo?>(null)
    private var massProcessingBoxesNumber by state<List<String>?>(null)
    private var exciseStampInfo by state<TaskExciseStampInfo?>(null)
    private var selectQualityCode by state<String?>(null)
    private var initialCount by state<String?>(null)
    private var isScan by state<Boolean?>(null)
    private var countAcceptRefusal by state<Double?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_excise_alco_box_card_pge

    override fun getPageNumber(): String = "09/43"

    override fun getViewModel(): ExciseAlcoBoxCardPGEViewModel {
        provideViewModel(ExciseAlcoBoxCardPGEViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = this.productInfo
            vm.selectQualityCode.value = this.selectQualityCode
            vm.initialCount.value = this.initialCount
            vm.isScan.value = this.isScan
            vm.countAcceptRefusal.value = this.countAcceptRefusal
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
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add) //в WM кнопка доступна всегда, хотя в https://trello.com/c/iOmIb6N7 для ситуации 2 прописаны условия
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)//в WM кнопка доступна всегда, хотя в https://trello.com/c/iOmIb6N7 для ситуации 2 прописаны условия

        connectLiveData(vm.enabledRollbackBtn, bottomToolbarUiModel.uiModelButton2.enabled)
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

}
