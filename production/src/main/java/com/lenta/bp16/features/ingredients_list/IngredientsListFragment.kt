package com.lenta.bp16.features.ingredients_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.*
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.provideViewModel

class IngredientsListFragment :
        CoreFragment<FragmentIgredientsListBinding, IngredientsListViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings, OnScanResultListener {

    private var byOrderRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var byMaterialRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_igredients_list
    }

    override fun getPageNumber(): String? {
        return SCREEN_NUMBER
    }

    override fun getViewModel(): IngredientsListViewModel {
        provideViewModel(IngredientsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
        vm.loadIngredients()
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        val marketNumber = vm.marketNumber
        topToolbarUiModel.title.value = getString(R.string.market_number, marketNumber)
        topToolbarUiModel.description.value = getString(R.string.desc_ingredients_list)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.stickers)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickMenu()
            R.id.b_3 -> {

            }
            R.id.b_5 -> vm.onRefreshClicked()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_BY_ORDER -> initIngredientsByOrderDataBinding(container)
            TAB_BY_MATERIALS -> initIngredientsByMaterialDataBinding(container)
            else -> View(context)
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_BY_ORDER -> getString(R.string.tab_name_by_order)
            TAB_BY_MATERIALS -> getString(R.string.tab_name_by_material)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return TAB_COUNTS
    }

    private fun initIngredientsByOrderDataBinding(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutIngredientsByOrderBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ingredients_by_order,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemIngredientByOrderBinding>(
                    layoutId = R.layout.item_ingredient_by_order,
                    itemId = BR.item,
                    onAdapterItemBind = { binding, position ->
                        byOrderRecyclerViewKeyHandler?.let {
                            binding.root.isSelected = it.isSelected(position)
                        }
                    },
                    onAdapterItemClicked = { position ->
                        byOrderRecyclerViewKeyHandler?.onItemClicked(position)
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            byOrderRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.rv,
                    items = vm.ingredientsByOrder,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickItemPosition
            )

            return layoutBinding.root
        }
    }

    private fun initIngredientsByMaterialDataBinding(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutIngredientsByMaterialBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ingredients_by_material,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemIngredientByMaterialBinding>(
                    layoutId = R.layout.item_ingredient_by_material,
                    itemId = BR.item,
                    onAdapterItemBind = { binding, position ->
                        byMaterialRecyclerViewKeyHandler?.let {
                            binding.root.isSelected = it.isSelected(position)
                        }
                    },
                    onAdapterItemClicked = { position ->
                        byMaterialRecyclerViewKeyHandler?.onItemClicked(position)
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            byMaterialRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.rv,
                    items = vm.ingredientsByMaterial,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickItemPosition
            )

            return layoutBinding.root
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        private const val TAB_COUNTS = 2
        private const val SCREEN_NUMBER = "16/82"

        const val TAB_BY_ORDER = 0
        const val TAB_BY_MATERIALS = 1
    }
}