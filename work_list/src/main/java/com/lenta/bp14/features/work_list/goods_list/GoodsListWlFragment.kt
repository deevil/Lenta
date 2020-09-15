package com.lenta.bp14.features.work_list.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.di.WorkListComponent
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.ui.ItemWorkListUi
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListWlFragment : KeyDownCoreFragment<FragmentGoodsListWlBinding, GoodsListWlViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_wl

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodsListWlViewModel {
        provideViewModel(GoodsListWlViewModel::class.java).let {
            CoreInjectHelper.getComponent(WorkListComponent::class.java)!!.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_list)

        connectLiveData(vm.taskName, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (vm.getCorrectedPagePosition(it) == GoodsListTab.SEARCH.position) {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.filter)
                } else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
                }
            })
        }

        connectLiveData(vm.middleButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> {
                if (vm.getCorrectedPagePosition(vm.selectedPage.value) == TAB_SEARCH) vm.onClickFilter()
                else vm.onClickDelete()
            }
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
        DataBindingUtil.inflate<LayoutWlGoodsListProcessingBinding>(LayoutInflater.from(container.context),
                R.layout.layout_wl_goods_list_processing,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemWorkListUi, ItemWlProcessingListBinding>(
                    layoutId = R.layout.item_wl_processing_list,
                    itemId = BR.good,
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
        DataBindingUtil.inflate<LayoutWlGoodsListProcessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_wl_goods_list_processed,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.processedSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_wl_processed_list,
                    itemId = BR.good,
                    onItemBind = { binding: ItemWlProcessedListBinding, position: Int ->
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
        DataBindingUtil.inflate<LayoutWlGoodsListSearchBinding>(LayoutInflater.from(container.context),
                R.layout.layout_wl_goods_list_search,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemWorkListUi, ItemWlSearchListBinding>(
                    layoutId = R.layout.item_wl_search_list,
                    itemId = BR.good,
                    keyHandlerId = TAB_SEARCH,
                    recyclerView = layoutBinding.rv,
                    items = vm.searchGoods,
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

    override fun onResume() {
        super.onResume()
        vm.updateGoodList()
    }

    companion object {
        const val SCREEN_NUMBER = "14"

        private const val TAB_PROCESSING = 0
        private const val TAB_PROCESSED = 1
        private const val TAB_SEARCH = 2
    }

}
