package com.lenta.movement.features.main.box

import android.os.Bundle
import android.view.View
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentGoodsListBinding
import com.lenta.movement.databinding.LayoutItemGoodsListBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListFragment : CoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
    OnScanResultListener,
    OnBackPresserListener,
    OnKeyDownListener,
    ToolbarButtonsClickListener {

    override fun getLayoutId() = R.layout.fragment_goods_list

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.cleanAll()
        topToolbarUiModel.title.value = getString(R.string.create_box_title)
        topToolbarUiModel.description.value = getString(R.string.create_box_product_list)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.completeEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onDeleteClick()
            R.id.b_5 -> vm.onCompleteClick()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onClickSelectionListener = View.OnClickListener { clickListener ->
            clickListener?.let {
                val position = it.tag as Int
                vm.selectionsHelper.revert(position = position)
                binding?.rv?.adapter?.notifyItemChanged(position)
            }
        }

        binding?.rvConfig = initRecycleAdapterDataBinding(
                layoutId = R.layout.layout_item_goods_list,
                itemId = BR.vm,
                onAdapterItemBind = { binding : LayoutItemGoodsListBinding , position ->
                    binding.tvCounter.tag = position
                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                    binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                    recyclerViewKeyHandler?.let {
                        binding.root.isSelected = it.isSelected(position)
                    }
                },
                onAdapterItemClicked = { position ->
                    recyclerViewKeyHandler?.let {
                        if (it.isSelected(position).not()) {
                            it.selectPosition(position)
                        }
                    }
                }

        )

        binding?.apply {
                recyclerViewKeyHandler = initRecyclerViewKeyHandler(
                        recyclerView = rv,
                        items = this@GoodsListFragment.vm.goodsList,
                        previousPosInfo = recyclerViewKeyHandler?.posInfo?.value
                )
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onResume() {
        vm.onResume()
        super.onResume()
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
    }
}