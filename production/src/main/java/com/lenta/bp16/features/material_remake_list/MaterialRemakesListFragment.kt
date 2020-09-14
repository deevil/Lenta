package com.lenta.bp16.features.material_remake_list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentRemakesByMaterialBinding
import com.lenta.bp16.databinding.ItemMaterialIngredientBinding
import com.lenta.bp16.databinding.ItemOrderIngredientBinding
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.ui.IngredientInfoUI
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class MaterialRemakesListFragment : CoreFragment<FragmentRemakesByMaterialBinding,
        MaterialRemakesListViewModel>(), OnBackPresserListener {

    // выбранный ранее ингредиент
    private val ingredientInfo: IngredientInfoUI by unsafeLazy {
        arguments?.getParcelable<IngredientInfoUI>(KEY_INGREDIENT_BY_MATERIAL)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT_BY_MATERIAL")
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_remakes_by_material
    }

    override fun getPageNumber(): String? {
        return SCREEN_NUMBER
    }

    override fun getViewModel(): MaterialRemakesListViewModel {
        provideViewModel(MaterialRemakesListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.ingredient.value = ingredientInfo
            return it
        }
    }

    override fun onResume() {
        super.onResume()
        vm.loadMaterialIngredients()
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        val material = ingredientInfo.getFormattedCode()
        val productName = ingredientInfo.text1.substring(6).trim()
        topToolbarUiModel.title.value = "$material $productName"
        topToolbarUiModel.description.value = getString(R.string.desc_remakes_list)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRvConfig()
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemMaterialIngredientBinding>(
                    layoutId = R.layout.item_material_ingredient,
                    itemId = BR.item
            )

            recyclerViewKeyHandler = initRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.rv,
                    items = vm.materialIngredients,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickItemPosition
            )
        }
    }

    companion object {
        private const val SCREEN_NUMBER = "16/89"
        private const val KEY_INGREDIENT_BY_MATERIAL = "KEY_INGREDIENT_BY_MATERIAL"

        fun newInstance(
                selectedIngredient: IngredientInfoUI
        ) = MaterialRemakesListFragment().apply {
            arguments = bundleOf(KEY_INGREDIENT_BY_MATERIAL to selectedIngredient)
        }
    }
}