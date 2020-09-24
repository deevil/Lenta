package com.lenta.bp9.features.goods_details

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentGoodsDetailsBinding
import com.lenta.bp9.databinding.ItemTileGoodsDetailsBinding
import com.lenta.bp9.databinding.ItemTileGoodsDetailsDelBinding
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class GoodsDetailsFragment : CoreFragment<FragmentGoodsDetailsBinding, GoodsDetailsViewModel>(), ToolbarButtonsClickListener {

    private var productInfo by state<TaskProductInfo?>(null)
    private var boxNumberForTaskPGEBoxAlco by state<String?>(null)
    private var isScreenPGEBoxAlcoInfo by state<Boolean?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_goods_details

    override fun getPageNumber(): String = "09/25"

    override fun getViewModel(): GoodsDetailsViewModel {
        provideViewModel(GoodsDetailsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            this.productInfo?.also { vm.initProduct(it) }
            this.boxNumberForTaskPGEBoxAlco?.also { vm.initBoxNumber(it) }
            this.isScreenPGEBoxAlcoInfo?.also { vm.initScreenPGEBoxAlcoInfo(it) }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.details_quantities_entered)
        topToolbarUiModel.title.value = vm.getTitle()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        if (vm.productInfo.value?.isSet == false) {
            bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
            connectLiveData(vm.enabledDelBtn, bottomToolbarUiModel.uiModelButton3.enabled)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (vm.productInfo.value?.isSet == false) {
            initRvConfigWithDel()
        } else {
            initRvConfig()
        }
    }

    //это view без удаления
    private fun initRvConfig() {
        binding
                ?.let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemTileGoodsDetailsBinding>(
                            layoutId = R.layout.item_tile_goods_details,
                            itemId = BR.item
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                }
    }

    //это view с удалением
    private fun initRvConfigWithDel() {
        binding?.let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.categoriesSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_tile_goods_details_del,
                    itemId = BR.item,
                    onItemBind = { binding: ItemTileGoodsDetailsDelBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.categoriesSelectionsHelper.isSelected(position)
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
        }
    }

    companion object {
        fun create(productInfo: TaskProductInfo, boxNumberForTaskPGEBoxAlco: String, isScreenPGEBoxAlcoInfo: Boolean): GoodsDetailsFragment {
            GoodsDetailsFragment().let {
                it.productInfo = productInfo
                it.boxNumberForTaskPGEBoxAlco = boxNumberForTaskPGEBoxAlco
                it.isScreenPGEBoxAlcoInfo = isScreenPGEBoxAlcoInfo
                return it
            }
        }
    }
}