package com.lenta.movement.features.task.basket

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskBasketBinding
import com.lenta.movement.databinding.LayoutItemGoodsListBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TaskBasketFragment: CoreFragment<FragmentTaskBasketBinding, TaskBasketViewModel>(),
    ToolbarButtonsClickListener,
    OnScanResultListener,
    OnKeyDownListener {

    private var basketIndex: Int? by state(null)

    companion object {
        fun newInstance(basketIndex: Int): TaskBasketFragment {
            return TaskBasketFragment().apply {
                this.basketIndex = basketIndex
            }
        }
    }

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId() = R.layout.fragment_task_basket

    override fun getPageNumber() = "13/06"

    override fun getViewModel(): TaskBasketViewModel {
        return provideViewModel(TaskBasketViewModel::class.java).also {
            getAppComponent()?.inject(it)
            it.basketIndex = basketIndex
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onClickSelectionListener = View.OnClickListener {
            (it!!.tag as Int).let { position ->
                vm.selectionsHelper.revert(position = position)
                binding?.recyclerView?.adapter?.notifyItemChanged(position)
            }
        }

        binding?.rvConfig = DataBindingRecyclerViewConfig(
            layoutId = R.layout.layout_item_goods_list,
            itemId = BR.vm,
            realisation = object : DataBindingAdapter<LayoutItemGoodsListBinding> {
                override fun onCreate(binding: LayoutItemGoodsListBinding) {
                    // do nothing
                }

                override fun onBind(binding: LayoutItemGoodsListBinding, position: Int) {
                    binding.tvCounter.tag = position
                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                    binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                    recyclerViewKeyHandler?.let {
                        binding.root.isSelected = it.isSelected(position)
                    }
                }
            },
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                recyclerViewKeyHandler?.let {
                    if (it.isSelected(position).not()) {
                        it.selectPosition(position)
                    }
                }
            }
        )

        recyclerViewKeyHandler = RecyclerViewKeyHandler(
            binding?.recyclerView!!,
            vm.goodsItemList,
            binding?.lifecycleOwner!!,
            recyclerViewKeyHandler?.posInfo?.value
        )
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.task_basket_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo(
            iconRes = R.drawable.ic_basket_24dp,
            titleRes = R.string.task_basket_characteristics_title
        ))
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

}