package com.lenta.bp10.features.write_off_details

import android.os.Bundle
import android.view.View
import com.lenta.bp10.BR
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentWriteOffDetailsBinding
import com.lenta.bp10.databinding.ItemTileGoodsReasonsBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class WriteOffDetailsFragment : CoreFragment<FragmentWriteOffDetailsBinding, WriteOffDetailsViewModel>(), ToolbarButtonsClickListener {

    private var productInfo: ProductInfo? = null

    override fun getLayoutId(): Int = R.layout.fragment_write_off_details

    override fun getPageNumber(): String = "10/08"

    override fun getViewModel(): WriteOffDetailsViewModel {
        provideViewModel(WriteOffDetailsViewModel::class.java).let { viewModel ->
            getAppComponent()?.inject(viewModel)
            productInfo?.let {
                viewModel.productInfoLiveData.value = it
            }
            return viewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvConfig = DataBindingRecyclerViewConfig(
                layoutId = R.layout.item_tile_goods_reasons,
                itemId = BR.vm,
                realisation = object : DataBindingAdapter<ItemTileGoodsReasonsBinding> {
                    override fun onCreate(binding: ItemTileGoodsReasonsBinding) {
                    }

                    override fun onBind(binding: ItemTileGoodsReasonsBinding, position: Int) {
                        binding.tvCounter.tag = position
                        binding.tvCounter.setOnClickListener {
                            (it!!.tag as Int).let { position ->
                                vm.selectionsHelper.revert(position = position)
                                this@WriteOffDetailsFragment.binding?.rv?.adapter?.notifyItemChanged(position)
                            }
                        }
                        binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                    }

                })
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        vm.productInfoLiveData.value?.let {
            topToolbarUiModel.title.value = "${it.getMaterialLastSix()} ${it.description}"
            topToolbarUiModel.description.value = getString(R.string.write_off_details)
        }


    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        viewLifecycleOwner.connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_3) {
            vm.onClickDelete()
        }
    }

    companion object {
        fun create(productInfo: ProductInfo): WriteOffDetailsFragment {
            return WriteOffDetailsFragment().apply {
                this.productInfo = productInfo
            }
        }
    }


}
