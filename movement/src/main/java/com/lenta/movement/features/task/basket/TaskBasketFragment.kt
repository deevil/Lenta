package com.lenta.movement.features.task.basket

import android.os.Bundle
import android.view.View
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskBasketBinding
import com.lenta.movement.databinding.LayoutItemSimpleBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TaskBasketFragment: CoreFragment<FragmentTaskBasketBinding, TaskBasketViewModel>(),
    ToolbarButtonsClickListener,
    OnScanResultListener,
    OnKeyDownListener {

    private var basketIndex: Int by state( DEFAULT_BASKET_INDEX )

    override fun getLayoutId() = R.layout.fragment_task_basket

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskBasketViewModel {
        return provideViewModel(TaskBasketViewModel::class.java).also {
            getAppComponent()?.inject(it)
            it.basketIndex.value = basketIndex
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            val vm = this@TaskBasketFragment.vm

            val onClickSelectionListener = View.OnClickListener { clickListener ->
                val itemPosition = clickListener.tag as Int
                vm.selectionsHelper.revert(position = itemPosition)
                this.recyclerView.adapter?.notifyItemChanged(itemPosition)
            }

            rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.layout_item_simple,
                    itemId = BR.item,
                    onAdapterItemBind = { binding: LayoutItemSimpleBinding, position ->
                        binding.tvCounter.tag = position
                        binding.tvCounter.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                        super.onAdapterBindHandler(binding, position)
                    }
            )

            recyclerViewKeyHandler = initRecyclerViewKeyHandler(
                    recyclerView = recyclerView,
                    items = vm.goodsItemList,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onItemClick
            )
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        connectLiveData(vm.title, topToolbarUiModel.title)
        topToolbarUiModel.description.value = getString(R.string.task_basket_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.properties)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onDeleteClick()
            R.id.b_4 -> vm.onCharacteristicsClick()
            R.id.b_5 -> vm.onNextClick()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        recyclerViewKeyHandler?.let {
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
        private const val DEFAULT_BASKET_INDEX = -1

        fun newInstance(basketIndex: Int): TaskBasketFragment {
            return TaskBasketFragment().apply {
                this.basketIndex = basketIndex
            }
        }
    }

}