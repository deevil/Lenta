package com.lenta.bp9.features.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListFragment : CoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
        ViewPagerSettings,
        OnScanResultListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    companion object {
        fun create(taskType: TaskType): GoodsListFragment {
            GoodsListFragment().let {
                it.taskType = taskType
                return it
            }
        }
    }

    private var taskType: TaskType = TaskType.None

    private var countedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var withoutBarcodeRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var toProcessingRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_goods_list

    override fun getPageNumber(): String =  if (this.taskType == TaskType.ShipmentPP) "09/111" else "09/15" //https://trello.com/c/3WVovfmE

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.taskType.value = this.taskType
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        if (this.taskType == TaskType.ShipmentPP) { //https://trello.com/c/3WVovfmE
            bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
            bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.skipAlternate)
            viewLifecycleOwner.apply {
                vm.selectedPage.observe(this, Observer {
                    if (it == 0) {
                        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing, enabled = vm.enabledBtnMissingForShipmentPP.value ?: false)
                        connectLiveData(vm.enabledBtnMissingForShipmentPP, bottomToolbarUiModel.uiModelButton4.enabled)
                    } else {
                        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.clean, enabled = vm.enabledBtnCleanForShipmentPP.value ?: false)
                        connectLiveData(vm.enabledBtnCleanForShipmentPP, bottomToolbarUiModel.uiModelButton4.enabled)
                    }
                })
            }
            connectLiveData(vm.enabledBtnSkipForShipmentPP, bottomToolbarUiModel.uiModelButton3.enabled)
            connectLiveData(vm.enabledBtnSaveForShipmentPP, bottomToolbarUiModel.uiModelButton5.enabled)
        } else {
            if (vm.isTaskPGE.value == false) {
                bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
            }
            bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean, visible = vm.visibilityCleanButton.value ?: true, enabled = vm.enabledCleanButton.value ?: false)
            bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.batches)

            connectLiveData(vm.visibilityCleanButton, bottomToolbarUiModel.uiModelButton3.visibility)
            connectLiveData(vm.enabledCleanButton, bottomToolbarUiModel.uiModelButton3.enabled)
            connectLiveData(vm.visibilityBatchesButton, bottomToolbarUiModel.uiModelButton4.visibility)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRefusal()
            R.id.b_3 -> vm.onClickThirdBtn()
            R.id.b_4 -> vm.onClickFourthBtn()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return if (taskType == TaskType.ShipmentPP) {
            when(position) {
                GoodsListViewPages.GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING -> prepareToProcessingView(container)
                GoodsListViewPages.GOODS_LIST_VIEW_PAGE_WITHOUT_BARCODE_OR_PROCESSED -> prepareProcessedView(container)
                else -> View(context)
            }
        } else {
            when(position) {
                GoodsListViewPages.GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING -> prepareCountedView(container)
                GoodsListViewPages.GOODS_LIST_VIEW_PAGE_WITHOUT_BARCODE_OR_PROCESSED -> prepareWithoutBarcodeView(container)
                else -> View(context)
            }
        }
    }

    private fun prepareCountedView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutGoodsListCountedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_list_counted,
                        container,
                        false)
                .let { layoutBinding ->
                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int)
                                .let { position ->
                                    vm.countedSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                    }

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_goods_list_counted,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileGoodsListCountedBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.countedSelectionsHelper.isSelected(position)
                                countedRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            },
                            onAdapterItemClicked = {position ->
                                countedRecyclerViewKeyHandler?.onItemClicked(position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    countedRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            previousPosInfo = countedRecyclerViewKeyHandler?.posInfo?.value,
                            items = vm.listCounted,
                            onClickHandler = vm::onClickItemPosition
                    )
                    return layoutBinding.root
                }
    }

    private fun prepareWithoutBarcodeView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutGoodsListWithoutBarcodeBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_list_without_barcode,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_goods_list_without_barcode,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileGoodsListWithoutBarcodeBinding, position: Int ->
                                withoutBarcodeRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            },
                            onAdapterItemClicked = {position ->
                                withoutBarcodeRecyclerViewKeyHandler?.onItemClicked(position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    withoutBarcodeRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            previousPosInfo = withoutBarcodeRecyclerViewKeyHandler?.posInfo?.value,
                            items = vm.listWithoutBarcode,
                            onClickHandler = vm::onClickItemPosition
                    )
                    return layoutBinding.root
                }
    }

    private fun prepareToProcessingView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutGoodsListToProcessingBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_list_to_processing,
                        container,
                        false)
                .let { layoutBinding ->
                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int)
                                .let { position ->
                                    vm.toProcessingSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                    }

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_goods_list_to_processing,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileGoodsListToProcessingBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.toProcessingSelectionsHelper.isSelected(position)
                                toProcessingRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            },
                            onAdapterItemClicked = {position ->
                                toProcessingRecyclerViewKeyHandler?.onItemClicked(position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    toProcessingRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            previousPosInfo = toProcessingRecyclerViewKeyHandler?.posInfo?.value,
                            items = vm.listToProcessing,
                            onClickHandler = vm::onClickItemPosition
                    )

                    return layoutBinding.root
                }
    }

    private fun prepareProcessedView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutGoodsListProcessedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_list_processed,
                        container,
                        false)
                .let { layoutBinding ->
                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int)
                                .let { position ->
                                    vm.processedSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                    }

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_goods_list_processed,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileGoodsListProcessedBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                                processedRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            },
                            onAdapterItemClicked = {position ->
                                processedRecyclerViewKeyHandler?.onItemClicked(position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    processedRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            previousPosInfo = processedRecyclerViewKeyHandler?.posInfo?.value,
                            items = vm.listProcessed,
                            onClickHandler = vm::onClickItemPosition
                    )

                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return if (taskType == TaskType.ShipmentPP) {
            getString(if (position == GoodsListViewPages.GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING) R.string.to_processing else R.string.processed)
        } else {
            getString(if (position == GoodsListViewPages.GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING) R.string.counted else R.string.without_barcode)
        }
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onResume() {
        super.onResume()
        if (vm.selectedPage.value == 0) {
            vm.requestFocusCountedOrToProcessing.value = true
        } else {
            vm.requestFocusWithoutBarcodeOrProcessed.value = true
        }
        vm.onResume()
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onFragmentResult(arguments: Bundle) {
        super.onFragmentResult(arguments)
        vm.onResult(arguments.getFragmentResultCode())
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

}
