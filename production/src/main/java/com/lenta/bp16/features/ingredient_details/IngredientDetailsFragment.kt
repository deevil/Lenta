package com.lenta.bp16.features.ingredient_details

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentIngredientDetailsBinding
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.model.ingredients.ui.OrderIngredientDataInfoUI
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class IngredientDetailsFragment : CoreFragment<FragmentIngredientDetailsBinding, IngredientDetailsViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener, OnScanResultListener {

    override fun getLayoutId(): Int {
        return R.layout.fragment_ingredient_details
    }

    override fun getPageNumber(): String {
        return SCREEN_NUMBER
    }

    private val orderIngredientDataInfo: OrderIngredientDataInfoUI by unsafeLazy {
        arguments?.getParcelable<OrderIngredientDataInfoUI>(KEY_INGREDIENT)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT")
    }

    private val eanInfo: OrderByBarcodeUI by unsafeLazy {
        arguments?.getParcelable<OrderByBarcodeUI>(KEY_EAN_INFO)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_EAN_INFO")
    }

    override fun getViewModel(): IngredientDetailsViewModel {
        provideViewModel(IngredientDetailsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.orderIngredient.value = orderIngredientDataInfo
            it.eanInfo.value = eanInfo
            it.parentCode = arguments?.getString(KEY_PARENT_CODE, "").orEmpty()
            return it
        }
    }

    init {
        lifecycleScope.launchWhenResumed {
            vm.requestFocusToNumberField.value = true
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
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.getWeight)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete, enabled = false)

        connectLiveData(vm.nextAndAddButtonEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.nextAndAddButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickGetWeight()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onCompleteClicked()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.updateData()
    }

    companion object {
        private const val SCREEN_NUMBER = "16/83"
        private const val KEY_INGREDIENT = "KEY_INGREDIENT"
        private const val KEY_PARENT_CODE = "KEY_PARENT_CODE"
        private const val KEY_EAN_INFO = "KEY_EAN_INFO"

        fun newInstance(selectedIngredient: OrderIngredientDataInfoUI, parentCode: String, eanInfo: OrderByBarcodeUI): IngredientDetailsFragment {
            return IngredientDetailsFragment().apply {
                arguments = bundleOf(KEY_INGREDIENT to selectedIngredient, KEY_PARENT_CODE to parentCode, KEY_EAN_INFO to eanInfo)
            }
        }
    }
}