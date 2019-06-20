package com.lenta.bp10.features.good_information.sets.component

import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentComponentBinding
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class ComponentFragment : CoreFragment<FragmentComponentBinding, ComponentViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener {

    private lateinit var productInfo: ProductInfo
    private lateinit var componentItem: ComponentItem

    companion object {
        fun create(productInfo: ProductInfo, componentItem: ComponentItem): ComponentFragment {
            ComponentFragment().let {
                it.productInfo = productInfo
                it.componentItem = componentItem
                return it
            }
        }

    }

    override fun getLayoutId(): Int = R.layout.fragment_component

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): ComponentViewModel {
        provideViewModel(ComponentViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.setProductInfo(productInfo)
            vm.setComponentItem(componentItem)
            vm.setLimitExceeded(getString(R.string.limit_exceeded))
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.set_component)
        topToolbarUiModel.title.value = componentItem.name
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback, enabled = false)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        viewLifecycleOwner.apply {
            connectLiveData(vm.enabledButton, bottomToolbarUiModel.uiModelButton4.enabled)
            connectLiveData(vm.enabledButton, bottomToolbarUiModel.uiModelButton5.enabled)
            connectLiveData(vm.enabledButton, bottomToolbarUiModel.uiModelButton2.enabled)
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

}
