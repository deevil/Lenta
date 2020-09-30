package com.lenta.bp10.features.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp10.BR
import com.lenta.bp10.R
import com.lenta.bp10.databinding.*
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListFragment : KeyDownCoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        OnScanResultListener,
        ToolbarButtonsClickListener,
        OnKeyDownListener {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list

    override fun getPageNumber() = generateScreenNumber()

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.print, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save, enabled = true)

        connectLiveData(source = vm.deleteEnabled, target = bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(source = vm.printButtonEnabled, target = bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(source = vm.saveButtonEnabled, target = bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
            //R.id.b_3 -> vm.onScanResult("010460606832927321P2XKAUcorIL6K91qrst92bqayYmJBoBksrEBIDkeDsADTYwzBIUqqmNdMXSJLWYjCFuNnzCGITHGVZWZfCIjhXZiYVJFSmyoPfduOeRgBWYoH")
            //R.id.b_3 -> vm.onScanResult("010460026601165721000001H800543025793psSa")
            //R.id.b_3 -> vm.onScanResult("010460026601165721000001G800512683393JEPq")
            R.id.b_4 -> vm.onClickPrint()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onFragmentResult(arguments: Bundle) {
        super.onFragmentResult(arguments)
        vm.onResult(arguments.getFragmentResultCode())
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_COUNTED -> initGoodListCounted(container)
            TAB_FILTER -> initGoodListFiltered(container)
            else -> View(context)
        }
    }

    private fun initGoodListCounted(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodsCountedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_goods_counted,
                container,
                false
        ).let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                val position = it.tag as Int
                vm.countedSelectionsHelper.revert(position)
                layoutBinding.rv.adapter?.notifyItemChanged(position)
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<GoodItem, ItemTileGoodsBinding>(
                    layoutId = R.layout.item_tile_goods,
                    itemId = BR.item,
                    onItemBind = { binding, position ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.countedSelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_COUNTED,
                    recyclerView = layoutBinding.rv,
                    items = vm.countedGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initGoodListFiltered(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodsFilterBinding>(LayoutInflater.from(container.context),
                R.layout.layout_goods_filter,
                container,
                false
        ).let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                val position = it.tag as Int
                vm.filteredSelectionsHelper.revert(position)
                layoutBinding.rv.adapter?.notifyItemChanged(position)
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<FilterItem, ItemTileFilterBinding>(
                    layoutId = R.layout.item_tile_filter,
                    itemId = BR.item,
                    onItemBind = { binding, position ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.filteredSelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_FILTER,
                    recyclerView = layoutBinding.rv,
                    items = vm.filteredGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_COUNTED -> getString(R.string.counted)
            TAB_FILTER -> getString(R.string.filter)
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
        private const val TABS = 2
        private const val TAB_COUNTED = 0
        private const val TAB_FILTER = 1
    }

}


