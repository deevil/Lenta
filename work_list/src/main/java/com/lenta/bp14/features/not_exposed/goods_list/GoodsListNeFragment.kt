package com.lenta.bp14.features.not_exposed.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.di.NotExposedComponent
import com.lenta.bp14.features.common_ui_model.SimpleProductUi
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.debounce
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListNeFragment : KeyDownCoreFragment<FragmentGoodsListNeBinding, GoodsListNeViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_ne

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodsListNeViewModel {
        provideViewModel(GoodsListNeViewModel::class.java).let {
            CoreInjectHelper.getComponent(NotExposedComponent::class.java)!!.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)
        topToolbarUiModel.title.value = vm.taskName
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        viewLifecycleOwner.apply {
            vm.correctedSelectedPage.observe(this, Observer {
                if (it == GoodsListTab.SEARCH.position) {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.filter)
                } else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
                }
            })
            vm.filterField.debounce().observe(this, Observer {
                vm.applyFilter()
            })
        }

        connectLiveData(vm.thirdButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.saveButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
        connectLiveData(vm.thirdButtonVisibility, bottomToolbarUiModel.uiModelButton3.visibility)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickThirdButton()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (vm.getCorrectedPagePosition(position)) {
            TAB_PROCESSING -> initProcessingGoodList(container)
            TAB_PROCESSED -> initProcessedGoodList(container)
            TAB_SEARCH -> initSearchGoodList(container)
            else -> View(context)
        }
    }

    private fun initProcessingGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutNeGoodsListProcessingBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ne_goods_list_processing,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<SimpleProductUi, ItemSimpleGoodBinding>(
                    layoutId = R.layout.item_simple_good,
                    itemId = BR.vm,
                    keyHandlerId = TAB_PROCESSING,
                    recyclerView = layoutBinding.rv,
                    items = vm.processingGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initProcessedGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutNeGoodsListProcessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ne_goods_list_processed,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.processedSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_good_quantity_selectable,
                    itemId = BR.vm,
                    onItemBind = { binding: ItemGoodQuantitySelectableBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_PROCESSED,
                    recyclerView = layoutBinding.rv,
                    items = vm.processedGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initSearchGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutNeGoodsListSearchBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ne_goods_list_search,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<SimpleProductUi, ItemNeFilteredGoodBinding>(
                    layoutId = R.layout.item_ne_filtered_good,
                    itemId = BR.vm,
                    keyHandlerId = TAB_SEARCH,
                    recyclerView = layoutBinding.rv,
                    items = vm.processingGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (vm.getCorrectedPagePosition(position)) {
            TAB_PROCESSING -> getString(R.string.processing)
            TAB_PROCESSED -> getString(R.string.processed)
            TAB_SEARCH -> getString(R.string.search)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return vm.getPagesCount()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        const val SCREEN_NUMBER = "74"

        private const val TAB_PROCESSING = 0
        private const val TAB_PROCESSED = 1
        private const val TAB_SEARCH = 2
    }

}
