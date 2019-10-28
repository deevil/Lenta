package com.lenta.bp9.features.discrepancy_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class DiscrepancyListFragment : CoreFragment<FragmentDiscrepancyListBinding, DiscrepancyListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener {

    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_discrepancy_list

    override fun getPageNumber(): String = "9/22"

    override fun getViewModel(): DiscrepancyListViewModel {
        provideViewModel(DiscrepancyListViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.titleProgressScreen.value = getString(R.string.data_loading)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.discrepancies_found)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.batches)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        connectLiveData(vm.visibilityCleanButton, bottomToolbarUiModel.uiModelButton3.visibility)
        connectLiveData(vm.enabledCleanButton, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.visibilityBatchesButton, bottomToolbarUiModel.uiModelButton4.visibility)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickClean()
            R.id.b_4 -> vm.onClickBatches()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutDiscrepancyListNotProcessedBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_discrepancy_list_not_processed,
                            container,
                            false).let { layoutBinding ->

                        val onClickGoodsTitle = View.OnClickListener {
                            (it!!.tag as Int).let { position ->
                                vm.onClickItemPosition(position)
                            }
                        }

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_discrepancy_list_not_processed,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileDiscrepancyListNotProcessedBinding> {
                                    override fun onCreate(binding: ItemTileDiscrepancyListNotProcessedBinding) {
                                    }

                                    override fun onBind(binding: ItemTileDiscrepancyListNotProcessedBinding, position: Int) {
                                        binding.tvCounter.tag = position
                                        binding.tvCounter.setOnClickListener(onClickGoodsTitle)
                                        binding.tvGoodsTitle.tag = position
                                        binding.tvGoodsTitle.setOnClickListener(onClickGoodsTitle)
                                    }

                                }
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutDiscrepancyListProcessedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_discrepancy_list_processed,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.processedSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_discrepancy_list_processed,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileDiscrepancyListProcessedBinding> {
                                override fun onCreate(binding: ItemTileDiscrepancyListProcessedBinding) {
                                }

                                override fun onBind(binding: ItemTileDiscrepancyListProcessedBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                                    processedRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                processedRecyclerViewKeyHandler?.let {
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
                    processedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.countProcessed,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = processedRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.not_processed else R.string.processed)

    override fun countTab(): Int = 2

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }


}
