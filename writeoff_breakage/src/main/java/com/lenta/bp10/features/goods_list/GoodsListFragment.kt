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
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListFragment :
        CoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        OnScanResultListener,
        ToolbarButtonsClickListener,
        OnKeyDownListener {

    private var countedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var filterRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_goods_list

    override fun getPageNumber() = "10/06"

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.setMsgGoodsNotForTask(getString(R.string.goods_not_for_task))
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

        viewLifecycleOwner.let {
            connectLiveData(source = vm.deleteEnabled, target = bottomToolbarUiModel.uiModelButton3.enabled)
            connectLiveData(source = vm.printButtonEnabled, target = bottomToolbarUiModel.uiModelButton4.enabled)
            connectLiveData(source = vm.saveButtonEnabled, target = bottomToolbarUiModel.uiModelButton5.enabled)
        }

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
        when (arguments.getInt("KEY_ARGS_ID_CODE_CONFIRM")) {
            0 -> vm.onConfirmAllDelete()
            1 -> vm.onSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {

        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutGoodsCountedBinding>(LayoutInflater.from(container.context),
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
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileGoodsBinding> {
                                    override fun onCreate(binding: ItemTileGoodsBinding) {
                                    }

                                    override fun onBind(binding: ItemTileGoodsBinding, position: Int) {
                                        binding.tvCounter.tag = position
                                        binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.countedSelectionsHelper.isSelected(position)
                                        countedRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                }
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        countedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.countedGoods,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutGoodsFilterBinding>(LayoutInflater.from(container.context),
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
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileFilterBinding> {
                                override fun onCreate(binding: ItemTileFilterBinding) {
                                }

                                override fun onBind(binding: ItemTileFilterBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.filteredSelectionsHelper.isSelected(position)
                                    filterRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }

                                }

                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    filterRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.filteredGoods,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!
                    )
                    return layoutBinding.root
                }


    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.counted else R.string.filter)

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected $position" }
        vm.onPageSelected(position)
    }

    override fun countTab(): Int = 2

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }


    override fun onKeyDown(keyCode: KeyCode): Boolean {
        (if (vm.selectedPage.value == 0) {
            countedRecyclerViewKeyHandler
        } else {
            filterRecyclerViewKeyHandler
        })?.let {
            return it.onKeyDown(keyCode)
        }
        return false
    }


}
