package com.lenta.inventory.features.goods_details_storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.inventory.BR
import com.lenta.inventory.R
import com.lenta.inventory.databinding.*
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsDetailsStorageFragment(private val productInfo: TaskProductInfo) : CoreFragment<FragmentGoodsDetailsStorageBinding, GoodsDetailsStorageViewModel>(),
        ToolbarButtonsClickListener,
        ViewPagerSettings,
        PageSelectionListener {

    private var countedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_goods_details_storage

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): GoodsDetailsStorageViewModel {
        provideViewModel(GoodsDetailsStorageViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = productInfo
            vm.isGeneralProduct.value = productInfo.type == ProductType.General || productInfo.type == ProductType.NonExciseAlcohol
            vm.isStorePlace.value = productInfo.placeCode != "00"
            vm.partly.value = getString(R.string.partly)
            vm.vintage.value = getString(R.string.vintage)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = when {
            vm.isGeneralProduct.value!! -> getString(R.string.goods_store_place)
            vm.isStorePlace.value!! -> getString(R.string.goods_of_details_store_place)
            else -> getString(R.string.goods_of_details)
        }
        topToolbarUiModel.title.value = "${productInfo.getMaterialLastSix()} ${productInfo.description}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)

        viewLifecycleOwner.apply {
            vm.selectedPage.observe(viewLifecycleOwner, Observer { pos ->
                if (pos == 0 && !vm.isGeneralProduct.value!! && !vm.productInfo.value!!.isSet) {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
                    connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
                }
                else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, visible = false)
                }
            })
        }
    }

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
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
        return if (vm.isGeneralProduct.value!! || vm.productInfo.value!!.isSet) {
                    when (position) {
                        0 -> createPagerItemNotProssed(container)
                        else -> createPagerItemProssed(container)
                    }
                }
                else {
                    if (vm.isStorePlace.value!!) {
                        when (position) {
                            0 -> createPagerItemCategories(container)
                            1 -> createPagerItemNotProssed(container)
                            else -> createPagerItemProssed(container)
                        }
                    }
                    else createPagerItemCategories(container)
                }
    }

    override fun getTextTitle(position: Int): String {
        return if (vm.isGeneralProduct.value!! || vm.productInfo.value!!.isSet) {
                    getString(
                            when (position) {
                                0 -> R.string.not_processed
                                else -> R.string.processed
                            })
                }
                else {
                    if (vm.isStorePlace.value!!) {
                        getString(
                                when (position) {
                                    0 -> R.string.categories
                                    1 -> R.string.not_processed
                                    else -> R.string.processed
                                })
                    }
                    else getString(R.string.categories)
                }
    }

    override fun countTab(): Int {
        return if (vm.isGeneralProduct.value!! || vm.productInfo.value!!.isSet) {
                    2
                }
                else {
                    if (vm.isStorePlace.value!!) 3 else 1
                }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    private fun createPagerItemCategories(container: ViewGroup): View{
        DataBindingUtil
                .inflate<LayoutGoodsDetailsCategoriesBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_details_categories,
                        container,
                        false).let {layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.categoriesSelectionsHelper.revert(position = position)
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
                                    binding.selectedForDelete = vm.categoriesSelectionsHelper.isSelected(position)
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
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = countedRecyclerViewKeyHandler?.posInfo?.value
                            )
                            return layoutBinding.root
                        }
            }

    private fun createPagerItemNotProssed(container: ViewGroup): View{
        DataBindingUtil
                .inflate<LayoutGoodsDetailsNotProssedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_details_not_prossed,
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

    private fun createPagerItemProssed(container: ViewGroup): View{
        DataBindingUtil
                .inflate<LayoutGoodsDetailsProssedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_details_prossed,
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
