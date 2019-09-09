package com.lenta.bp14.features.price_check.good_info

import androidx.lifecycle.Observer
import com.lenta.bp14.R
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.databinding.FragmentGoodInfoPcBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodInfoPcFragment : CoreFragment<FragmentGoodInfoPcBinding, GoodInfoPcViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_good_info_pc

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("42")

    override fun getViewModel(): GoodInfoPcViewModel {
        provideViewModel(GoodInfoPcViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)

        viewLifecycleOwner.apply {
            vm.good.observe(this, Observer { good ->
                if (good != null) {
                    topToolbarUiModel.title.value = "${good.getFormattedMaterial()} ${good.name}"
                }
            })
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.error)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.correct)
    }

}
