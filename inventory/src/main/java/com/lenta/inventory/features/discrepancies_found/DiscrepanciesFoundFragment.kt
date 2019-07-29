package com.lenta.inventory.features.discrepancies_found

import android.content.Context
import android.os.Bundle
import android.transition.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.inventory.BR
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentDiscrepanciesFoundBinding
import com.lenta.inventory.databinding.ItemTileDiscrepanciesBinding
import com.lenta.inventory.databinding.LayoutDiscrepanciesBinding
import com.lenta.inventory.databinding.LayoutDiscrepanciesByStorageBinding
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class DiscrepanciesFoundFragment : CoreFragment<FragmentDiscrepanciesFoundBinding, DiscrepanciesFoundViewModel>(), ToolbarButtonsClickListener, ViewPagerSettings, PageSelectionListener {

    private var byGoodsRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var byStoragePlaceRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_discrepancies_found
    override fun getPageNumber(): String = "11/10"

    override fun getViewModel(): DiscrepanciesFoundViewModel {
        provideViewModel(DiscrepanciesFoundViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.discrepancies_found)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(if (vm.selectedPage.value == 0) ButtonDecorationInfo.delete else ButtonDecorationInfo.untie)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.skip)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickDeleteUntie()
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickSkip()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }
        viewLifecycleOwner.apply {
            connectLiveData(source = vm.absentEnabled, target = getBottomToolBarUIModel()!!.uiModelButton4.enabled)
            connectLiveData(source = vm.untieDeleteEnabled, target = getBottomToolBarUIModel()!!.uiModelButton2.enabled)
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.byGoods else R.string.byStorage)

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected $position" }
        vm.onPageSelected(position)
        invalidateBottomToolBar()
    }

    override fun countTab(): Int {
        if (vm.isRecountByStorePlaces()) {
            return 2
        } else {
            return 1
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutDiscrepanciesBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_discrepancies,
                            container,
                            false).let { layoutBinding ->

                        val onClickSelectionListener = View.OnClickListener {
                            (it!!.tag as Int).let { position ->
                                vm.byGoodsSelectionHelper.revert(position = position)
                                layoutBinding.rv.adapter?.notifyItemChanged(position)
                            }
                        }

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_discrepancies,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileDiscrepanciesBinding> {
                                    override fun onCreate(binding: ItemTileDiscrepanciesBinding) {
                                    }

                                    override fun onBind(binding: ItemTileDiscrepanciesBinding, position: Int) {
                                        binding.tvCounter.tag = position
                                        if (vm.isRecountByStorePlaces()) {
                                            binding.imageView2.visibility = View.INVISIBLE
                                        } else {
                                            binding.imageView2.visibility = View.VISIBLE
                                        }
                                        binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.byGoodsSelectionHelper.isSelected(position)
                                        byGoodsRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    byGoodsRecyclerViewKeyHandler?.let {
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
                        byGoodsRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.discrepanciesByGoods,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = byGoodsRecyclerViewKeyHandler?.posInfo?.value
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutDiscrepanciesByStorageBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_discrepancies_by_storage,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.byStorageSelectionHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_discrepancies,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileDiscrepanciesBinding> {
                                override fun onCreate(binding: ItemTileDiscrepanciesBinding) {
                                }

                                override fun onBind(binding: ItemTileDiscrepanciesBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.byStorageSelectionHelper.isSelected(position)
                                    byStoragePlaceRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                byStoragePlaceRecyclerViewKeyHandler?.let {
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
                    byStoragePlaceRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.discrepanciesByStorage,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = byStoragePlaceRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }
}