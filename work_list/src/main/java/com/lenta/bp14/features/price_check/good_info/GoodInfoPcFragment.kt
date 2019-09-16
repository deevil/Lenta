package com.lenta.bp14.features.price_check.good_info

import android.view.View
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentGoodInfoPcBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodInfoPcFragment : CoreFragment<FragmentGoodInfoPcBinding, GoodInfoPcViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_info_pc

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("42")

    override fun getViewModel(): GoodInfoPcViewModel {
        provideViewModel(GoodInfoPcViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.goods_info)

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.noPrice)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.errorPrice)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.rightPrice)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickNoPrice()
            R.id.b_4 -> vm.onClickNotValid()
            R.id.b_5 -> vm.onClickValid()
        }
    }




}
