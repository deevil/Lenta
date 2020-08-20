package com.lenta.bp16.features.order_details

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentOrderDetailsBinding
import com.lenta.bp16.features.material_remake_details.MaterialRemakeDetailsFragment
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.ui.OrderByBarcode
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class OrderDetailsFragment : CoreFragment<FragmentOrderDetailsBinding, OrderDetailsViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_details
    }

    override fun getPageNumber(): String {
        return SCREEN_NUMBER
    }

    private val ingredientInfo: IngredientInfo by unsafeLazy {
        arguments?.getParcelable<IngredientInfo>(KEY_INGREDIENT)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT")
    }

    private val eanInfo: OrderByBarcode by unsafeLazy {
        arguments?.getParcelable<OrderByBarcode>(KEY_EAN_INFO)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_EAN_INFO")
    }

    override fun getViewModel(): OrderDetailsViewModel {
        provideViewModel(OrderDetailsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.ingredient.value = ingredientInfo
            it.eanInfo.value = eanInfo
            return it
        }
    }

    init {
        lifecycleScope.launchWhenResumed {
            vm.requestFocusToCount.value = true
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = ingredientInfo.text3
        topToolbarUiModel.description.value = getString(R.string.desc_ingredient_detail)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

    companion object {
        private const val SCREEN_NUMBER = "16/83"
        private const val KEY_INGREDIENT = "KEY_INGREDIENT"
        private const val KEY_EAN_INFO = "KEY_EAN_INFO"

        fun newInstance(selectedIngredient: IngredientInfo,eanInfo: OrderByBarcode): OrderDetailsFragment {
            return OrderDetailsFragment().apply {
                arguments = bundleOf(KEY_INGREDIENT to selectedIngredient,
                KEY_EAN_INFO to eanInfo)
            }
        }
    }
}