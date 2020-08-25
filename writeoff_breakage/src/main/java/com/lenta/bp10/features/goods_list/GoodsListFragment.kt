package com.lenta.bp10.features.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.bp10.BR
import com.lenta.bp10.R
import com.lenta.bp10.databinding.*
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListFragment : CoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        OnScanResultListener,
        ToolbarButtonsClickListener,
        OnKeyDownListener {

    private var countedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var filterRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

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
                false).let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.countedSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_tile_goods,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemTileGoodsBinding> {
                        override fun onCreate(binding: ItemTileGoodsBinding) {
                        }

                        override fun onBind(binding: ItemTileGoodsBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.countedSelectionsHelper.isSelected(position)
                            countedRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    },
                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        countedRecyclerViewKeyHandler?.let {
                            if (it.isSelected(position)) {
                                vm.onClickItemPosition(position)
                            } else {
                                it.selectPosition(position)
                            }
                        }
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            countedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.countedGoods,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = countedRecyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    private fun initGoodListFiltered(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodsFilterBinding>(LayoutInflater.from(container.context),
                R.layout.layout_goods_filter,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.filteredSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_tile_filter,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemTileFilterBinding> {
                        override fun onCreate(binding: ItemTileFilterBinding) {
                        }

                        override fun onBind(binding: ItemTileFilterBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.filteredSelectionsHelper.isSelected(position)
                            filterRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    },
                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        filterRecyclerViewKeyHandler?.let {
                            if (it.isSelected(position)) {
                                vm.onClickItemPosition(position)
                            } else {
                                it.selectPosition(position)
                            }
                        }
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            filterRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.filteredGoods,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = filterRecyclerViewKeyHandler?.posInfo?.value
            )

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

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        return when (vm.selectedPage.value) {
            TAB_COUNTED -> countedRecyclerViewKeyHandler
            else -> null
        }?.onKeyDown(keyCode) ?: false
    }

    companion object {
        private const val TABS = 2
        private const val TAB_COUNTED = 0
        private const val TAB_FILTER = 1
    }

}


