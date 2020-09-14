package com.lenta.inventory.features.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.inventory.BR
import com.lenta.inventory.R
import com.lenta.inventory.databinding.*
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class GoodsListFragment : KeyDownCoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings, PageSelectionListener, OnScanResultListener, OnBackPresserListener {

    private var storePlaceNumber by state("00")

    override fun getLayoutId(): Int = R.layout.fragment_goods_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.setStorePlaceNumber(storePlaceNumber)
            return vm
        }
    }

    override fun onFragmentResult(arguments: Bundle) {
        super.onFragmentResult(arguments)
        vm.onResult(arguments.getFragmentResultCode())
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == 0) {
                    if (vm.isStrict()) {
                        bottomToolbarUiModel.uiModelButton3.clean()
                    } else {
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
                    }
                } else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
                }
            })
        }
        connectLiveData(source = vm.deleteEnabled, target = bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(source = vm.onCompleteButtonEnabled, target = bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickClean()
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onClickBack()
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_PROCESSING -> initProcessingGoodList(container)
            TAB_PROCESSED -> initProcessedGoodList(container)
            else -> View(context)
        }
    }

    private fun initProcessingGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodsUnprocessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_goods_unprocessed,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.unprocessedSelectionHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = if (vm.isStrict()) {
                initRecycleAdapterDataBinding(
                        layoutId = R.layout.item_tile_goods,
                        itemId = BR.item,
                        onItemBind = { binding: ItemTileGoodsBinding, position: Int ->
                            binding.tvItemNumber.tag = position
                        },
                        keyHandlerId = TAB_PROCESSING,
                        recyclerView = layoutBinding.rv,
                        items = vm.unprocessedGoods,
                        onClickHandler = vm::onClickItemPosition
                )
            } else {
                initRecycleAdapterDataBinding(
                        layoutId = R.layout.item_tile_processed_goods,
                        itemId = BR.item,
                        onItemBind = { binding: ItemTileProcessedGoodsBinding, position: Int ->
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.tvPlan.visibility = View.INVISIBLE
                            binding.selectedForDelete = vm.unprocessedSelectionHelper.isSelected(position)
                        },
                        keyHandlerId = TAB_PROCESSING,
                        recyclerView = layoutBinding.rv,
                        items = vm.unprocessedGoods,
                        onClickHandler = vm::onClickItemPosition
                )
            }

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initProcessedGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodsProcessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_goods_processed,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.processedSelectionHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_tile_processed_goods,
                    itemId = BR.item,
                    onItemBind = { binding: ItemTileProcessedGoodsBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.processedSelectionHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_PROCESSED,
                    recyclerView = layoutBinding.rv,
                    items = vm.unprocessedGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_PROCESSING -> getString(R.string.not_processed)
            TAB_PROCESSED -> getString(R.string.processed)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected $position" }
        vm.onPageSelected(position)
    }

    override fun countTab(): Int = TABS

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        fun create(storePlaceNumber: String): GoodsListFragment {
            val fragment = GoodsListFragment()
            fragment.storePlaceNumber = storePlaceNumber
            return fragment
        }

        const val SCREEN_NUMBER = "08"

        private const val TABS = 2
        private const val TAB_PROCESSING = 0
        private const val TAB_PROCESSED = 1
    }

}