package com.lenta.bp16.features.ingredient_details.add_attribute

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentIngredientAttributeBinding
import com.lenta.bp16.features.ingredient_details.IngredientDetailsFragment
import com.lenta.bp16.features.material_remake_details.add_attribute.MaterialAttributeFragment
import com.lenta.bp16.features.material_remake_details.add_attribute.MaterialAttributeViewModel
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class IngredientAttributeFragment : CoreFragment<FragmentIngredientAttributeBinding, IngredientAttributeViewModel>(), ToolbarButtonsClickListener {

    private val parentCode: String by unsafeLazy {
        arguments?.getString(KEY_PARENT_CODE)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_PARENT_CODE")
    }

    private val orderIngredientDataInfo: OrderIngredientDataInfo by unsafeLazy {
        arguments?.getParcelable<OrderIngredientDataInfo>(KEY_INGREDIENT)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT")
    }

    override fun getLayoutId(): Int = R.layout.fragment_ingredient_attribute

    override fun getPageNumber(): String = SCREEN_NUMBER

    override fun getViewModel(): IngredientAttributeViewModel {
        provideViewModel(IngredientAttributeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.orderIngredient.value = orderIngredientDataInfo
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = buildString {
            append(orderIngredientDataInfo.getFormattedMaterial())
            append(" ")
            append(orderIngredientDataInfo.name)
        }
        topToolbarUiModel.description.value = arguments?.getString(KEY_PARENT_CODE, "").orEmpty()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.updateData()
        vm.getServerTime()
    }

    companion object {
        private const val SCREEN_NUMBER = "16/83"
        private const val KEY_INGREDIENT = "KEY_INGREDIENT"
        private const val KEY_PARENT_CODE = "KEY_PARENT_CODE"

        fun newInstance(selectedIngredient: OrderIngredientDataInfo, parentCode: String) = IngredientAttributeFragment().apply {
            arguments = bundleOf(
                    KEY_INGREDIENT to selectedIngredient,
                    KEY_PARENT_CODE to parentCode
            )
        }
    }
}