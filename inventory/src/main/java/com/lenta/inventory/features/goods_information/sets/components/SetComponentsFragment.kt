package com.lenta.inventory.features.goods_information.sets.components

import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentSetComponentsBinding
import com.lenta.inventory.features.goods_details.ComponentItem
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class SetComponentsFragment : CoreFragment<FragmentSetComponentsBinding, SetComponentsViewModel>(), ToolbarButtonsClickListener {

    private var productInfo by state<ProductInfo?>(null)
    private var componentItem by state<ComponentItem?>(null)

    companion object {
        fun create(productInfo: ProductInfo, componentItem: ComponentItem): SetComponentsFragment {
            SetComponentsFragment().let {
                it.productInfo = productInfo
                it.componentItem = componentItem
                return it
            }
        }

    }

    override fun getLayoutId(): Int = R.layout.fragment_set_components

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): SetComponentsViewModel {
        provideViewModel(SetComponentsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            productInfo?.let {
                vm.setProductInfo(it)
            }
            componentItem?.let {
                vm.setComponentItem(it)
            }
            vm.setLimitExceeded(getString(R.string.limit_exceeded))
            vm.spinList.value = listOf("Марочно")
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.set_component)
        componentItem?.let {
            topToolbarUiModel.title.value = it.name
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback, enabled = false)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        viewLifecycleOwner.let {
            connectLiveData(vm.enabledButton, bottomToolbarUiModel.uiModelButton4.enabled)
            connectLiveData(vm.enabledButton, bottomToolbarUiModel.uiModelButton5.enabled)
            connectLiveData(vm.enabledButton, bottomToolbarUiModel.uiModelButton2.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

}
