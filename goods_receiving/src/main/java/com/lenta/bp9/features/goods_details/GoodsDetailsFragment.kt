package com.lenta.bp9.features.goods_details

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentGoodsDetailsBinding
import com.lenta.bp9.databinding.ItemTileGoodsDetailsBinding
import com.lenta.bp9.databinding.ItemTileGoodsDetailsDelBinding
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class GoodsDetailsFragment : CoreFragment<FragmentGoodsDetailsBinding, GoodsDetailsViewModel>(), ToolbarButtonsClickListener {

    companion object {
        fun create(productInfo: TaskProductInfo?, batch: TaskBatchInfo?): GoodsDetailsFragment {
            GoodsDetailsFragment().let {
                it.productInfo = productInfo
                it.batch = batch
                return it
            }
        }
    }

    private var productInfo by state<TaskProductInfo?>(null)
    private var batch by state<TaskBatchInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_goods_details

    override fun getPageNumber(): String = "09/25"

    override fun getViewModel(): GoodsDetailsViewModel {
        provideViewModel(GoodsDetailsViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            productInfo?.let {
                vm.productInfo.value = it
            }
            batch?.let {
                vm.batchInfo.value = it
            }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.details_quantities_entered)
        topToolbarUiModel.title.value = "${vm.productInfo.value!!.getMaterialLastSix()} ${vm.productInfo.value!!.description}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        /**это для view с/без удаления, ранее использовалась, но решили сделать для всех товаров с возможностью удаления количеств по расхождениям
          if (vm.isVetProduct.value == true) {
            bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
            connectLiveData(vm.enabledDelBtn, bottomToolbarUiModel.uiModelButton3.enabled)
        }*/
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        connectLiveData(vm.enabledDelBtn, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**это для view с/без удаления, ранее использовалась, но решили сделать для всех товаров с возможностью удаления количеств по расхождениям
          if (vm.isVetProduct.value == true) {
            initRvConfigWithDel()
        } else {
            initRvConfig()
        }*/
        initRvConfigWithDel()
    }

    //это view без удаления, ранее использовалась, но решили сделать для всех товаров с возможностью удаления количеств по расхождениям
    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_tile_goods_details,
                    itemId = BR.vm,
                    realisation = object : DataBindingAdapter<ItemTileGoodsDetailsBinding> {
                        override fun onCreate(binding: ItemTileGoodsDetailsBinding) {
                        }

                        override fun onBind(binding: ItemTileGoodsDetailsBinding, position: Int) {
                        }

                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun initRvConfigWithDel() {
        binding?.let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.categoriesSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_tile_goods_details_del,
                    itemId = BR.vm,
                    realisation = object : DataBindingAdapter<ItemTileGoodsDetailsDelBinding> {
                        override fun onCreate(binding: ItemTileGoodsDetailsDelBinding) {
                        }

                        override fun onBind(binding: ItemTileGoodsDetailsDelBinding, position: Int) {
                            binding.tvCounter.tag = position
                            binding.tvCounter.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.categoriesSelectionsHelper.isSelected(position)
                        }

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

}
