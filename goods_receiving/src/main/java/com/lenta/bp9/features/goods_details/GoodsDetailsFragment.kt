package com.lenta.bp9.features.goods_details

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentGoodsDetailsBinding
import com.lenta.bp9.databinding.ItemTileGoodsDetailsBinding
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class GoodsDetailsFragment : CoreFragment<FragmentGoodsDetailsBinding, GoodsDetailsViewModel>() {

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
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

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

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

}
