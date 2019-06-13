package com.lenta.bp10.features.good_information.excise_alco

import android.os.Bundle
import android.view.View
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.features.good_information.general.GoodInfoFragment
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.setVisible
import com.lenta.shared.utilities.extentions.toStringFormatted


class ExciseAlcoInfoFragment : GoodInfoFragment() {

    override fun getViewModel(): BaseProductInfoViewModel {
        provideViewModel(ExciseAlcoInfoViewModel::class.java).let { viewModel ->
            getAppComponent()?.inject(viewModel)
            productInfo?.let {
                viewModel.setProductInfo(it)
            }
            initCount?.let {
                viewModel.count.value = it.toStringFormatted()
            }
            return viewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply { bScanEs.setVisible(true) }
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
