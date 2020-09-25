package com.lenta.bp10.features.good_information.sets.component

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp10.R
import com.lenta.bp10.features.good_information.excise_alco.ExciseAlcoInfoFragment
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.setVisible
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.state.state

class ComponentFragment : ExciseAlcoInfoFragment() {

    private var componentItem by state<ComponentItem?>(null)
    private var targetTotalCount by state<Double?>(null)

    private var componentViewModel: ComponentViewModel? = null

    override fun getViewModel(): ComponentViewModel {
        provideViewModel(ComponentViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            productInfo?.let {
                vm.setProductInfo(it)
            }
            componentItem?.let {
                vm.setComponentItem(it)
            }

            targetTotalCount?.let {
                vm.setTargetTotalCount(it)
            }

            componentViewModel = vm
            vm.initExciseAlcoDelegate()
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

        componentViewModel?.let {
            connectLiveData(it.rollBackEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.bScan?.setVisible(false)
        vm.totalCount.observe(viewLifecycleOwner, Observer {
            binding?.tvTotalValue?.text = getString(R.string.one_of, it.toStringFormatted(), componentViewModel?.getTargetCount())
        })
    }


    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> componentViewModel?.onClickRollBack()
            else -> super.onToolbarButtonClick(view)
        }
    }

    companion object {
        fun newInstance(productInfo: ProductInfo, componentItem: ComponentItem, targetTotalCount: Double): ComponentFragment {
            ComponentFragment().let {
                it.productInfo = productInfo
                it.componentItem = componentItem
                it.targetTotalCount = targetTotalCount
                return it
            }
        }
    }
}
