package com.lenta.inventory.features.goods_information.general

import android.os.Bundle
import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentGoodsInfoBinding
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class GoodsInfoFragment : CoreFragment<FragmentGoodsInfoBinding,
        GoodsInfoViewModel>(),
        ToolbarButtonsClickListener,
        OnScanResultListener {

    companion object {
        fun create(productInfo: TaskProductInfo): GoodsInfoFragment {
            GoodsInfoFragment().let {
                it.productInfo = productInfo
                return it
            }
        }

    }

    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_goods_info

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): GoodsInfoViewModel {
        provideViewModel(GoodsInfoViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = this.productInfo
            vm.spinList.value = listOf(getString(R.string.quantity))
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
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomToolbarUiModel = getBottomToolBarUIModel()!!
        viewLifecycleOwner.apply {
            connectLiveData(vm.isStorePlaceNumber, bottomToolbarUiModel.uiModelButton3.visibility)
            connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

}
