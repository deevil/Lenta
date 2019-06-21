package com.lenta.bp10.features.good_information.sets.component

import android.os.Bundle
import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.features.good_information.excise_alco.ExciseAlcoInfoFragment
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.setVisible

class ComponentFragment : ExciseAlcoInfoFragment() {

    private lateinit var componentItem: ComponentItem
    private var componentViewModel: ComponentViewModel? = null

    companion object {
        fun create(productInfo: ProductInfo, componentItem: ComponentItem): ComponentFragment {
            ComponentFragment().let {
                it.productInfo = productInfo
                it.componentItem = componentItem
                return it
            }
        }

    }


    override fun getViewModel(): ComponentViewModel {
        provideViewModel(ComponentViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.setProductInfo(productInfo!!)
            vm.setComponentItem(componentItem)
            componentViewModel = vm
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        super.setupTopToolBar(topToolbarUiModel)
        topToolbarUiModel.description.value = getString(R.string.set_component)
    }


    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        super.setupBottomToolBar(bottomToolbarUiModel)
        bottomToolbarUiModel.uiModelButton3.visibility.value = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.bScan.setVisible(false)
        }
    }


    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> componentViewModel?.onClickRollBack()
            else -> super.onToolbarButtonClick(view)
        }
    }


}
