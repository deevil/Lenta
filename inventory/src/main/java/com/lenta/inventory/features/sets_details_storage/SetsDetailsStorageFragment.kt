package com.lenta.inventory.features.sets_details_storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.inventory.BR
import com.lenta.inventory.R
import com.lenta.inventory.databinding.*
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class SetsDetailsStorageFragment : CoreFragment<FragmentSetsDetailsStorageBinding, SetsDetailsStorageViewModel>(),
        ViewPagerSettings,
        PageSelectionListener {

    companion object {
        fun create(productInfo: ProductInfo): SetsDetailsStorageFragment {
            SetsDetailsStorageFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

    //private var productInfo by state<ProductInfo?>(null)
    private var productInfo = ProductInfo("000021", "Виски для киски", Uom("ST", "шт"), ProductType.General,
            false, "1", MatrixType.Active, "materialType")


    override fun getLayoutId(): Int = R.layout.fragment_sets_details_storage

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): SetsDetailsStorageViewModel {
        provideViewModel(SetsDetailsStorageViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            productInfo?.let {
                vm.setProductInfo(it)
            }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_of_details_mx)
        productInfo?.let {
            topToolbarUiModel.title.value = "${it.getMaterialLastSix()} ${it.description}"
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }

    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {

        when (position) {
            0 -> {
                DataBindingUtil
                        .inflate<LayoutSetsDetailsNotProssedBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_sets_details_not_prossed,
                                container,
                                false).let { layoutBinding ->

                            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                    layoutId = R.layout.item_tile_goods_details_storage,
                                    itemId = BR.vm,
                                    realisation = object : DataBindingAdapter<ItemTileGoodsDetailsStorageBinding> {
                                        override fun onCreate(binding: ItemTileGoodsDetailsStorageBinding) {
                                        }

                                        override fun onBind(binding: ItemTileGoodsDetailsStorageBinding, position: Int) {
                                            binding.tvCounter.tag = position
                                        }

                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            return layoutBinding.root
                        }

            }
            else -> {
                DataBindingUtil
                        .inflate<LayoutSetsDetailsProssedBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_sets_details_prossed,
                                container,
                                false).let { layoutBinding ->

                            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                    layoutId = R.layout.item_tile_goods_details_storage,
                                    itemId = BR.vm,
                                    realisation = object : DataBindingAdapter<ItemTileGoodsDetailsStorageBinding> {
                                        override fun onCreate(binding: ItemTileGoodsDetailsStorageBinding) {
                                        }

                                        override fun onBind(binding: ItemTileGoodsDetailsStorageBinding, position: Int) {
                                            binding.tvCounter.tag = position
                                        }

                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            return layoutBinding.root
                        }
            }
        }

    }

    override fun getTextTitle(position: Int): String = getString(
            when (position) {
                0 -> R.string.not_processed
                else -> R.string.processed
            })

    override fun countTab(): Int = 2

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }


}
