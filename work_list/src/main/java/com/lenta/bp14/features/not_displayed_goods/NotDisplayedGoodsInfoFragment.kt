package com.lenta.bp14.features.not_displayed_goods

import com.lenta.bp14.R
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import androidx.databinding.DataBindingUtil
import com.lenta.bp14.BR
import com.lenta.bp14.databinding.*
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig

class NotDisplayedGoodsInfoFragment : CoreFragment<FragmentNotDisplayedGoodsInfoBinding, NotDisplayedGoodsInfoViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_not_displayed_goods_info

    override fun getPageNumber(): String {
        return "14/75"
    }

    override fun getViewModel(): NotDisplayedGoodsInfoViewModel {
        provideViewModel(NotDisplayedGoodsInfoViewModel::class.java).let {
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
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.cancel)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.framed)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.not_framed)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            0 -> DataBindingUtil
                    .inflate<LayoutNotDisplayedCommonInfoBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_not_displayed_common_info,
                            container,
                            false).apply {
                        this.vm = vm
                        this.lifecycleOwner = viewLifecycleOwner
                    }.root
            else -> DataBindingUtil
                    .inflate<LayoutNotDisplayedGoodsBalancesBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_not_displayed_goods_balances,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemTileExpirationBinding>(
                                layoutId = R.layout.item_tile_stock,
                                itemId = BR.vm
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        return layoutBinding.root
                    }
        }
    }

    override fun getTextTitle(position: Int): String {
        return getString(if (position == 0) R.string.common_info else R.string.stocks_list_title)
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}
