package com.lenta.bp16.features.material_remake_details.add_attribute

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentMaterialAttributeBinding
import com.lenta.bp16.model.ingredients.ui.MaterialIngredientDataInfoUI
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class MaterialAttributeFragment : CoreFragment<FragmentMaterialAttributeBinding, MaterialAttributeViewModel>(), ToolbarButtonsClickListener {

    private val parentCode: String by unsafeLazy {
        arguments?.getString(KEY_PARENT_CODE)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_PARENT_CODE")
    }

    private val materialIngredientDataInfo: MaterialIngredientDataInfoUI by unsafeLazy {
        arguments?.getParcelable<MaterialIngredientDataInfoUI>(KEY_INGREDIENT)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT")
    }

    override fun getLayoutId(): Int = R.layout.fragment_material_attribute

    override fun getPageNumber(): String = SCREEN_NUMBER

    override fun getViewModel(): MaterialAttributeViewModel {
        provideViewModel(MaterialAttributeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.materialIngredient.value = materialIngredientDataInfo
            it.parentCode = arguments?.getString(KEY_PARENT_CODE, "").orEmpty()
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = buildString {
            append(parentCode)
            append(" ")
            append(materialIngredientDataInfo.name)
        }
        topToolbarUiModel.description.value = materialIngredientDataInfo.ltxa1
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

        fun newInstance(selectedIngredient: MaterialIngredientDataInfoUI, parentCode: String) = MaterialAttributeFragment().apply {
            arguments = bundleOf(
                    KEY_INGREDIENT to selectedIngredient,
                    KEY_PARENT_CODE to parentCode
            )
        }
    }
}