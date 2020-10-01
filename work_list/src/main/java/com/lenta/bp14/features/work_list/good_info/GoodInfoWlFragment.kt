package com.lenta.bp14.features.work_list.good_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.di.WorkListComponent
import com.lenta.bp14.platform.extentions.getHelperComponent
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodInfoWlFragment : CoreFragment<FragmentGoodInfoWlBinding, GoodInfoWlViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_info_wl

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("15")

    override fun getViewModel(): GoodInfoWlViewModel = provideViewModel(GoodInfoWlViewModel::class.java).apply {
        getHelperComponent<WorkListComponent>()?.inject(this)
    }


    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.deliveries)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.sales)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        connectLiveData(vm.applyButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.openGoodDetails()
            //R.id.b_2 -> vm.onScanResult("010460606832927321P2XKAUcorIL6K91qrst92bqayYmJBoBksrEBIDkeDsADTYwzBIUqqmNdMXSJLWYjCFuNnzCGITHGVZWZfCIjhXZiYVJFSmyoPfduOeRgBWYoH") // 521445
            R.id.b_3 -> vm.openGoodDeliveries()
            R.id.b_4 -> vm.openGoodSales()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View = when (position) {
        0 -> {
            DataBindingUtil.inflate<LayoutWlGoodInfoCommonBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_info_common,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner

                vm.dateFields = listOf(layoutBinding.dayField, layoutBinding.monthField, layoutBinding.yearField)

                layoutBinding.root
            }
        }

        1 -> {
            DataBindingUtil.inflate<LayoutWlGoodInfoAdditionalBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_info_additional,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner

                layoutBinding.root
            }
        }

        2 -> {
            DataBindingUtil.inflate<LayoutWlGoodInfoProvidersBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_info_providers,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodProviderPeriodBinding>(
                        layoutId = R.layout.item_good_provider_period,
                        itemId = BR.provider)

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner

                layoutBinding.root
            }
        }

        3 -> {
            DataBindingUtil.inflate<LayoutWlGoodInfoStocksBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_info_stocks,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemStorageStockBinding>(
                        layoutId = R.layout.item_storage_stock,
                        itemId = BR.stock)

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner

                layoutBinding.root
            }
        }

        4 -> {
            DataBindingUtil.inflate<LayoutWlGoodPartsStocksBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_parts_stocks,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodZPartBinding>(
                        layoutId = R.layout.item_good_z_part,
                        itemId = BR.zPart)

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner

                layoutBinding.root
            }
        }

        else -> throw IllegalArgumentException("Wrong pager position!")
    }


    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.common_good_info_title)
            1 -> getString(R.string.additional_good_info_title)
            2 -> getString(R.string.good_providers_list_title)
            3 -> getString(R.string.stocks_list_title)
            4 -> getString(R.string.parts)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return if (vm.additional.value?.hasZParts == true) {
            5
        } else {
            4
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            viewPagerSettings = this@GoodInfoWlFragment
            vm?.additional?.observe(viewLifecycleOwner) {
                viewPager.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
//        private const val
    }

}