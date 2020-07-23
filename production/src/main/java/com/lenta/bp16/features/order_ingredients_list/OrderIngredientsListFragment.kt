package com.lenta.bp16.features.order_ingredients_list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentIngredientsByOrderBinding
import com.lenta.bp16.databinding.ItemOrderIngredientBinding
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.state.state

class OrderIngredientsListFragment : CoreFragment<FragmentIngredientsByOrderBinding,
        OrderIngredientsListViewModel>(), OnBackPresserListener {

    // выбранный ранее ингредиент
    private val ingredientInfo: IngredientInfo by unsafeLazy {
        arguments?.getParcelable<IngredientInfo>(KEY_INGREDIENT)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT")
    }

    // Вес, количество ингредиентов
    var weight: String by state("")

    override fun getLayoutId(): Int {
        return R.layout.fragment_ingredients_by_order
    }

    override fun getPageNumber(): String? {
        return SCREEN_NUMBER
    }

    override fun getViewModel(): OrderIngredientsListViewModel {
        provideViewModel(OrderIngredientsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.weight = weight
            it.ingredient.value = ingredientInfo
            return it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRvConfig()
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = ingredientInfo.text3.orEmpty()
        topToolbarUiModel.description.value = getString(R.string.desc_order_ingredients_list_by, weight)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemOrderIngredientBinding>(
                    layoutId = R.layout.item_order_ingredient,
                    itemId = BR.item
            )

            recyclerViewKeyHandler = initRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.rv,
                    items = vm.orderIngredientsList,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickItemPosition
            )
        }
    }

    companion object {
        private const val SCREEN_NUMBER = "16/87"
        private const val KEY_INGREDIENT = "KEY_INGREDIENT"

        fun newInstance(
                weightCount: String,
                selectedIngredient: IngredientInfo
        ) = OrderIngredientsListFragment().apply {
            weight = weightCount
            arguments = bundleOf(KEY_INGREDIENT to selectedIngredient)
        }
    }
}