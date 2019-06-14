package com.lenta.inventory.features.goods_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.inventory.BR
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentGoodsDetailsBinding
import com.lenta.inventory.databinding.ItemTileGoodsDetailsCategoriesBinding
import com.lenta.inventory.databinding.LayoutGoodsDetailsBinding
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsDetailsFragment : CoreFragment<FragmentGoodsDetailsBinding, GoodsDetailsViewModel>(),
        ToolbarButtonsClickListener,
        ViewPagerSettings,
        PageSelectionListener {

    companion object {
        fun create(productInfo: ProductInfo): GoodsDetailsFragment {
            GoodsDetailsFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

    //private var productInfo by state<ProductInfo?>(null)
    private var productInfo = ProductInfo("000021", "Виски для киски", Uom("ST", "шт"), ProductType.General,
            false, "1", MatrixType.Active, "materialType")

    private var countedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_goods_details

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): GoodsDetailsViewModel {
        provideViewModel(GoodsDetailsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            productInfo?.let {
                vm.setProductInfo(it)
            }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_of_details)
        productInfo?.let {
            topToolbarUiModel.title.value = "${it.getMaterialLastSix()} ${it.description}"
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        viewLifecycleOwner.connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_3) {
            vm.onClickDelete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }

    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {

        DataBindingUtil
                .inflate<LayoutGoodsDetailsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_details,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.countedSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }


                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_goods_details_categories,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileGoodsDetailsCategoriesBinding> {
                                override fun onCreate(binding: ItemTileGoodsDetailsCategoriesBinding) {
                                }

                                override fun onBind(binding: ItemTileGoodsDetailsCategoriesBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.countedSelectionsHelper.isSelected(position)
                                    countedRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }

                                }

                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    countedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.countedCategories,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!
                    )
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(R.string.categories)

    override fun countTab(): Int = 1

    override fun onPageSelected(position: Int) {
        return
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }


}
