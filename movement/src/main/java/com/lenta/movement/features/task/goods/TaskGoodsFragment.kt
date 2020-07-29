package com.lenta.movement.features.task.goods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskGoodsBinding
import com.lenta.movement.databinding.LayoutItemSimpleBinding
import com.lenta.movement.databinding.LayoutTaskGoodsBucketsTabBinding
import com.lenta.movement.databinding.LayoutTaskGoodsProcessedTabBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.movement.view.simpleListRecyclerViewConfig
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
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
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskGoodsFragment : CoreFragment<FragmentTaskGoodsBinding, TaskGoodsViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnBackPresserListener,
        OnScanResultListener,
        OnKeyDownListener {

    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var basketRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId() = R.layout.fragment_task_goods

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskGoodsViewModel {
        return provideViewModel(TaskGoodsViewModel::class.java).also {
            getAppComponent()?.inject(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.task_goods_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.saveEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onDeleteClick()
            R.id.b_5 -> vm.onSaveClick()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (TaskGoodsPage.values()[position]) {
            TaskGoodsPage.PROCESSED -> {
                DataBindingUtil.inflate<LayoutTaskGoodsProcessedTabBinding>(
                        LayoutInflater.from(context),
                        R.layout.layout_task_goods_processed_tab,
                        container,
                        false
                ).apply {
                    val vm = this@TaskGoodsFragment.vm

                    val onClickSelectionListener = View.OnClickListener { clickListener ->
                        val itemPosition = clickListener.tag as Int
                        vm.processedSelectionHelper.revert(position = itemPosition)
                        processedRecyclerView.adapter?.notifyItemChanged(itemPosition)
                    }

                    rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.layout_item_simple,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: LayoutItemSimpleBinding, position ->
                                binding.tvCounter.tag = position
                                binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.processedSelectionHelper.isSelected(position)
                                binding.root.isSelected = processedRecyclerView.isSelected
                            }
                    )

                    this.vm = vm

                    processedRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = processedRecyclerView,
                            items = vm.processedList,
                            previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                            onClickHandler = vm::onClickProcessedItem
                    )

                }.root
            }
            TaskGoodsPage.BASKETS -> {
                DataBindingUtil.inflate<LayoutTaskGoodsBucketsTabBinding>(
                        LayoutInflater.from(context),
                        R.layout.layout_task_goods_buckets_tab,
                        container,
                        false
                ).also { dataBinding ->
                    dataBinding?.rvConfig = simpleListRecyclerViewConfig(
                            recyclerView = dataBinding?.basketRecyclerView,
                            selectionItemsHelper = vm.basketSelectionHelper,
                            recyclerViewKeyHandler = basketRecyclerViewKeyHandler,
                            onClickItem = { position -> vm.onClickBasketItem(position) }
                    )

                    dataBinding.vm = vm
                    dataBinding.lifecycleOwner = binding?.lifecycleOwner

                    dataBinding?.basketRecyclerView?.let { recyclerView ->
                        binding?.lifecycleOwner?.let { lifecycleOwner ->
                            basketRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    recyclerView,
                                    vm.basketItemList,
                                    lifecycleOwner,
                                    basketRecyclerViewKeyHandler?.posInfo?.value
                            )
                        }
                    }

                }.root
            }
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (TaskGoodsPage.values()[position]) {
            TaskGoodsPage.PROCESSED -> getString(R.string.task_goods_processed_tab_title)
            TaskGoodsPage.BASKETS -> getString(R.string.task_goods_buckets_tab_title)
        }
    }

    override fun countTab() = TaskGoodsPage.values().size

    override fun onScanResult(data: String) = vm.onScanResult(data)

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        when (vm.currentPage.value) {
            TaskGoodsPage.PROCESSED -> processedRecyclerViewKeyHandler
            TaskGoodsPage.BASKETS -> basketRecyclerViewKeyHandler
            else -> null
        }?.let {
            if (!it.onKeyDown(keyCode)) {
                keyCode.digit?.let { digit ->
                    vm.onDigitPressed(digit)
                    return true
                }
                return false
            }
            return true
        }
        return false
    }

    companion object {
        private const val PAGE_NUMBER = "13/06"
    }
}