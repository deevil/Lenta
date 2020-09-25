package com.lenta.bp10.features.good_information.general

import android.os.Bundle
import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentGoodInfoBinding
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

open class GoodInfoFragment : CoreFragment<FragmentGoodInfoBinding, BaseProductInfoViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener, OnScanResultListener {

    protected var productInfo by state<ProductInfo?>(null)

    protected var initCount by state<Double?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_good_info

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): BaseProductInfoViewModel {
        provideViewModel(GoodInfoViewModel::class.java).let { viewModel ->
            getAppComponent()?.inject(viewModel)
            productInfo?.let {
                viewModel.setProductInfo(it)
            }
            initCount?.let {
                viewModel.initCount(it)
                initCount = null
            }
            return viewModel
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
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
        connectLiveData(vm.enabledDetailsButton, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onBackPressed(): Boolean {
        return vm.onBackPressed()
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onFragmentResult(arguments: Bundle) {
        vm.handleFragmentResult(arguments.getFragmentResultCode())

    }

    override fun onResume() {
        super.onResume()
        vm.updateCounter()
        vm.requestFocusToQuantity.value = true
    }

    companion object {
        fun newInstance(productInfo: ProductInfo, initCount: Double): GoodInfoFragment {
            GoodInfoFragment().let {
                it.productInfo = productInfo
                it.initCount = initCount
                return it
            }
        }
    }

}
