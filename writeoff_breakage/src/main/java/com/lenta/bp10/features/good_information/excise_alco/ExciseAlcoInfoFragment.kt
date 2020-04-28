package com.lenta.bp10.features.good_information.excise_alco

import android.os.Bundle
import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.features.good_information.general.GoodInfoFragment
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.extentions.*


open class ExciseAlcoInfoFragment : GoodInfoFragment() {
    var exciseAlcoInfoViewModel: ExciseAlcoInfoViewModel? = null

    override fun getViewModel(): BaseProductInfoViewModel {
        provideViewModel(ExciseAlcoInfoViewModel::class.java).let { viewModel ->
            getAppComponent()?.inject(viewModel)
            exciseAlcoInfoViewModel = viewModel
            viewModel.setProductInfo(productInfo!!)
            initCount?.let {
                viewModel.initCount(it)
                initCount = null
            }
            return viewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            bScanEs.setVisible(true)
            etWriteOff.disable()
            spinnerWriteOffType.requestFocus()
        }
    }

    override fun onRequestFocus() {
        binding?.apply {
            spinnerWriteOffType.requestFocus()
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        super.setupBottomToolBar(bottomToolbarUiModel)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        exciseAlcoInfoViewModel?.let {
            connectLiveData(it.rollBackEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
        }


    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> exciseAlcoInfoViewModel?.onClickRollBack()
            else -> super.onToolbarButtonClick(view)
        }

    }

    companion object {
        fun create(productInfo: ProductInfo): ExciseAlcoInfoFragment {
            ExciseAlcoInfoFragment().let {
                it.productInfo = productInfo
                it.initCount = 0.0
                return it
            }
        }

    }
}
