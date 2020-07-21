package com.lenta.bp16.features.ingredient_details

import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentOrderDetailsBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class IngredientDetailsFragment : CoreFragment<FragmentOrderDetailsBinding, IngredientDetailsViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_details
    }

    override fun getPageNumber(): String {
        return SCREEN_NUMBER
    }

    private val ingredientInfo: OrderIngredientDataInfo by unsafeLazy {
        arguments?.getParcelable<OrderIngredientDataInfo>(KEY_INGREDIENT)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT")
    }

    override fun getViewModel(): IngredientDetailsViewModel {
        provideViewModel(IngredientDetailsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.orderIngredient.value = ingredientInfo
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = ingredientInfo.matnr
        topToolbarUiModel.description.value = getString(R.string.desc_ingredient_detail)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.getWeight)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_4 -> vm.onIngredientClicked()
            R.id.b_5 -> vm.onCompleteClicked()
        }
    }

    companion object {
        private const val SCREEN_NUMBER = "16/83"
        private const val KEY_INGREDIENT = "KEY_INGREDIENT"

        fun newInstance(selectedIngredient: OrderIngredientDataInfo): IngredientDetailsFragment {
            return IngredientDetailsFragment().apply {
                arguments = bundleOf(KEY_INGREDIENT to selectedIngredient)
            }
        }
    }
}