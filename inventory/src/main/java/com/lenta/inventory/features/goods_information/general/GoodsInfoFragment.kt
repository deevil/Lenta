package com.lenta.inventory.features.goods_information.general

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

class GoodsInfoFragment(val productInfo: TaskProductInfo, val storePlaceNumber: String) : CoreFragment<FragmentGoodsInfoBinding, GoodsInfoViewModel>(), ToolbarButtonsClickListener, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_goods_info

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): GoodsInfoViewModel {
        provideViewModel(GoodsInfoViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = this.productInfo
            vm.storePlaceNumber.value = this.storePlaceNumber
            vm.spinList.value = listOf(getString(R.string.quantity))
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)
        productInfo?.let {
            topToolbarUiModel.title.value = "${it.getMaterialLastSix()} ${it.description}"
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        viewLifecycleOwner.apply {
            connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
        }
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

}
