package com.lenta.bp14.features.work_list.good_info

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
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.databinding.*
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix

class GoodInfoWlFragment : CoreFragment<FragmentGoodInfoWlBinding, GoodInfoWlViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_good_info_wl

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("15")

    override fun getViewModel(): GoodInfoWlViewModel {
        provideViewModel(GoodInfoWlViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)

        vm.good.observe(this, Observer<Good> { good ->
            topToolbarUiModel.title.value = "${good.getFormattedMaterial()} ${good.name}"
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.deliveries)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.sales)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil.inflate<LayoutWlGoodInfoCommonBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_info_common,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                return layoutBinding.root
            }
        }

        if (position == 1) {
            DataBindingUtil.inflate<LayoutWlGoodInfoAdditionalBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_info_additional,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                return layoutBinding.root
            }
        }

        if (position == 2) {
            DataBindingUtil.inflate<LayoutWlGoodInfoProvidersBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_info_providers,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodProviderPeriodBinding>(
                        layoutId = R.layout.item_good_provider_period,
                        itemId = BR.vm)

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                return layoutBinding.root
            }
        }

        DataBindingUtil.inflate<LayoutWlGoodInfoStocksBinding>(LayoutInflater.from(container.context),
                R.layout.layout_wl_good_info_stocks,
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
            0 -> getString(R.string.common_good_info_title)
            1 -> getString(R.string.additional_good_info_title)
            2 -> getString(R.string.good_providers_list_title)
            3 -> getString(R.string.stocks_list_title)
            else -> {
                Logg.d { "Wrong pager position!" }
                "Error"
            }
        }
    }

    override fun countTab() = 4

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}