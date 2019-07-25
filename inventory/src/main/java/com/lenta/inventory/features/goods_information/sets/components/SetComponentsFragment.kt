package com.lenta.inventory.features.goods_information.sets.components

import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentSetComponentsBinding
import com.lenta.inventory.features.goods_information.sets.SetComponentInfo
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
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

class SetComponentsFragment : CoreFragment<FragmentSetComponentsBinding, SetComponentsViewModel>(),
        OnBackPresserListener,
        OnScanResultListener,
        ToolbarButtonsClickListener {

    private var targetTotalCount by state<Double?>(null)
    private var componentInfo by state<SetComponentInfo?>(null)

    companion object {
        fun create(componentInfo: SetComponentInfo, targetTotalCount: Double): SetComponentsFragment {
            SetComponentsFragment().let {
                it.targetTotalCount = targetTotalCount
                it.componentInfo = componentInfo
                return it
            }
        }

    }

    override fun getLayoutId(): Int = R.layout.fragment_set_components

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): SetComponentsViewModel {
        provideViewModel(SetComponentsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.targetTotalCount.value = targetTotalCount
            vm.componentInfo.value = componentInfo
            vm.limitExceeded.value = getString(R.string.limit_exceeded)
            vm.spinList.value = listOf(getString(R.string.quantity))
            vm.titleProgressScreen.value = getString(R.string.data_loading)
            vm.stampAnotherProduct.value = getString(R.string.stamp_another_product)
            vm.alcocodeNotFound.value = getString(R.string.alcocode_not_found)
            vm.componentNotFound.value = getString(R.string.component_not_found)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.set_component)
        viewLifecycleOwner.let {
            connectLiveData(vm.topTitle, topToolbarUiModel.title)
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        viewLifecycleOwner.let {
            connectLiveData(vm.enabledRollbackButton, bottomToolbarUiModel.uiModelButton2.enabled)
            connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

}
