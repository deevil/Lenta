package com.lenta.bp14.features.not_exposed.good_info

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
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig

class GoodInfoNeFragment : CoreFragment<FragmentGoodInfoNeBinding, GoodInfoNeViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_good_info_ne

    override fun getPageNumber(): String {
        return "14/75"
    }

    override fun getViewModel(): GoodInfoNeViewModel {
        provideViewModel(GoodInfoNeViewModel::class.java).let {
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
        if (position == 0) {
            DataBindingUtil.inflate<LayoutNeGoodInfoCommonBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_ne_good_info_common,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                return layoutBinding.root
            }
        }

        DataBindingUtil.inflate<LayoutNeGoodInfoStocksBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ne_good_info_stocks,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemStorageStockBinding>(
                    layoutId = R.layout.item_storage_stock,
                    itemId = BR.vm)

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.common_info)
            1 -> getString(R.string.stocks_list_title)
            else -> {
                Logg.d { "Wrong pager position!" }
                "Error"
            }
        }
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}