package com.lenta.bp14.features.work_list.good_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodInfoWlFragment : CoreFragment<FragmentGoodInfoWlBinding, GoodInfoWlViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener {

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

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.deliveries)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.sales)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.openGoodDetails()
            R.id.b_3 -> vm.openGoodDeliveries()
            R.id.b_4 -> vm.openGoodSales()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil.inflate<LayoutWlGoodInfoCommonBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_good_info_common,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.vm = vm
                layoutBinding.good = vm.good.value
                layoutBinding.common = vm.good.value?.common
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
                        itemId = BR.provider)

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
                    itemId = BR.stock)

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
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab() = 4

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
        binding?.options = vm.good.value?.common?.options

        initSpinners()
    }

    private fun initSpinners() {
        val comments = mutableListOf("Не выбран")
        vm.good.value?.common?.serverComments?.map { comments.add(it) }
        vm.commentsList.value = comments

        vm.shelfLifeTypeList.value = resources.getStringArray(R.array.shelf_life_type).asList()
    }

}
