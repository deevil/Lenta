package com.lenta.inventory.features.goods_information.excise_alco

import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentExciseAlcoInfoBinding
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class ExciseAlcoInfoFragment : CoreFragment<FragmentExciseAlcoInfoBinding, ExciseAlcoInfoViewModel>(), ToolbarButtonsClickListener, OnScanResultListener {

    companion object {
        fun create(productInfo: TaskProductInfo): ExciseAlcoInfoFragment {
            ExciseAlcoInfoFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_excise_alco_info

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): ExciseAlcoInfoViewModel {
        provideViewModel(ExciseAlcoInfoViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = productInfo
            vm.spinList.value = listOf(getString(R.string.quantity), getString(R.string.partly), getString(R.string.vintage))
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)
        topToolbarUiModel.title.value = "${vm.productInfo.value!!.getMaterialLastSix()} ${vm.productInfo.value!!.description}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        /**viewLifecycleOwner.apply {
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
        connectLiveData(vm.enabledDetailsButton, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.selectedPosition, bottomToolbarUiModel.uiModelButton4.requestFocus)
        }*/
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

}
