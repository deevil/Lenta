package com.lenta.bp16.features.tech_orders_list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentTechOrdersListBinding
import com.lenta.bp16.databinding.ItemTechOrderBinding
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.ui.ItemTechOrderUi
import com.lenta.bp16.model.ingredients.ui.MaterialIngredientDataInfoUI
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class TechOrdersListFragment : CoreFragment<FragmentTechOrdersListBinding, TechOrdersListViewModel>() {

    private val materialIngredientDataInfo: MaterialIngredientDataInfoUI by unsafeLazy {
        arguments?.getParcelable<MaterialIngredientDataInfoUI>(KEY_INGREDIENT)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT")
    }

    private val parentCode: String by unsafeLazy {
        arguments?.getString(KEY_PARENT_CODE)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_PARENT_CODE")
    }

    private val materialIngredientKtsch: String by unsafeLazy {
        arguments?.getString(KEY_INGREDIENT_KTSCH)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT_KTSCH")
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_tech_orders_list
    }

    override fun getPageNumber(): String? {
        return SCREEN_NUMBER
    }

    override fun getViewModel(): TechOrdersListViewModel {
        provideViewModel(TechOrdersListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.materialIngredientKtsch.value = materialIngredientKtsch
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = buildString {
            append(materialIngredientDataInfo.ltxa1)
            append(" / ")
            append(materialIngredientDataInfo.lgort)
        }
        topToolbarUiModel.description.value = getString(R.string.desc_tech_order_list_by)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemTechOrderUi, ItemTechOrderBinding>(
                    layoutId = R.layout.item_tech_order,
                    itemId = BR.item,
                    recyclerView = layoutBinding.rv,
                    items = vm.allTechOrdersList
            )
        }
    }

    companion object {
        private const val SCREEN_NUMBER = "16/93"
        private const val KEY_INGREDIENT = "KEY_INGREDIENT"
        private const val KEY_PARENT_CODE = "KEY_PARENT_CODE"
        private const val KEY_INGREDIENT_KTSCH = "KEY_INGREDIENT_KTSCH"

        fun newInstance(selectedIngredient: MaterialIngredientDataInfoUI, parentCode: String, materialIngredientKtsch: String) =
                TechOrdersListFragment().apply {
                    arguments = bundleOf(
                            KEY_INGREDIENT to selectedIngredient,
                            KEY_PARENT_CODE to parentCode,
                            KEY_INGREDIENT_KTSCH to materialIngredientKtsch
                    )
                }
    }
}