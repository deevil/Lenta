package com.lenta.bp14.features.not_exposed.good_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.di.NotExposedComponent
import com.lenta.shared.di.CoreInjectHelper.getComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
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

class GoodInfoNeFragment : CoreFragment<FragmentGoodInfoNeBinding, GoodInfoNeViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnBackPresserListener, OnScanResultListener {
    //todo: партии
    override fun getLayoutId(): Int = R.layout.fragment_good_info_ne

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("75")

    override fun getViewModel(): GoodInfoNeViewModel {
        provideViewModel(GoodInfoNeViewModel::class.java).let {
            getComponent(NotExposedComponent::class.java)!!.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)
        topToolbarUiModel.title.value = vm.getTitle()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.cancel)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.framed)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.not_framed)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.cancelButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton2.enabled)
        connectLiveData(vm.framedButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
        connectLiveData(vm.notFramedButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton4.enabled)
        connectLiveData(vm.applyButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickCancel()
            R.id.b_3 -> vm.onClickFramed()
            R.id.b_4 -> vm.onClickNotFramed()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    private fun getCommonGoodInfoView(container: ViewGroup): View {
        return DataBindingUtil.inflate<LayoutNeGoodInfoCommonBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ne_good_info_common,
                container,
                false).run {
            vm = this@GoodInfoNeFragment.vm
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    private fun getGoodInfoStocksView(container: ViewGroup): View {
        return DataBindingUtil.inflate<LayoutNeGoodInfoStocksBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ne_good_info_stocks,
                container,
                false).run {
            rvConfig = initRecycleAdapterDataBinding<ItemStorageStockBinding>(
                    layoutId = R.layout.item_storage_stock,
                    itemId = BR.stock,
                    onItemBind = { binding, index ->
                        binding.layoutStorage.setOnClickListener {
                            this@GoodInfoNeFragment.vm.onStockItemClick(index)
                        }
                    }
            )

            vm = this@GoodInfoNeFragment.vm
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    private fun getGoodZPartsListView(container: ViewGroup):View {
        return DataBindingUtil.inflate<LayoutNeGoodPartsStocksBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ne_good_parts_stocks,
                container,
                false).run {
            rvConfig = initRecycleAdapterDataBinding<ItemGoodZPartBinding>(
                    layoutId = R.layout.item_good_z_part,
                    itemId = BR.zPart
            )

            vm = this@GoodInfoNeFragment.vm
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View = when (position) {
        FIRST_ITEM_POSITION -> getCommonGoodInfoView(container)
        SECOND_ITEM_POSITION -> getGoodInfoStocksView(container)
        THIRD_ITEM_POSITION -> getGoodZPartsListView(container)
        else -> throw IllegalArgumentException(WRONG_PAGER_POSITION_MESSAGE)
    }


    override fun getTextTitle(position: Int): String {
        return when (position) {
            FIRST_ITEM_POSITION -> getString(R.string.common_info)
            SECOND_ITEM_POSITION -> getString(R.string.stocks_list_title)
            THIRD_ITEM_POSITION -> getString(R.string.parts)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return if(vm.goodInfo.hasZParts) {
            THREE_ITEMS_SIZE
        } else {
            TWO_ITEMS_SIZE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onBackPressed(): Boolean {
        return vm.onBackPressed()
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        private const val FIRST_ITEM_POSITION = 0
        private const val SECOND_ITEM_POSITION = 1
        private const val THIRD_ITEM_POSITION = 2
        private const val TWO_ITEMS_SIZE = 2
        private const val THREE_ITEMS_SIZE = 3
        private const val WRONG_PAGER_POSITION_MESSAGE = "Wrong pager position!"
    }

}
