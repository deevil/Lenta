package com.lenta.bp9.features.transport_marriage.goods_info

import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentTransportMarriageGoodsInfoBinding
import com.lenta.bp9.model.task.TaskTransportMarriageInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TransportMarriageGoodsInfoFragment : CoreFragment<FragmentTransportMarriageGoodsInfoBinding, TransportMarriageGoodsInfoViewModel>(),
        ToolbarButtonsClickListener,
        OnScanResultListener {

    companion object {
        fun create(transportMarriageInfo: TaskTransportMarriageInfo): TransportMarriageGoodsInfoFragment {
            TransportMarriageGoodsInfoFragment().let {
                it.transportMarriageInfo = transportMarriageInfo
                return it
            }
        }
    }

    private var transportMarriageInfo by state<TaskTransportMarriageInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_transport_marriage_goods_info

    override fun getPageNumber(): String = "09/16"

    override fun getViewModel(): TransportMarriageGoodsInfoViewModel {
        provideViewModel(TransportMarriageGoodsInfoViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.transportMarriageInfoCurrent.value = this.transportMarriageInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.goods_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
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
}
