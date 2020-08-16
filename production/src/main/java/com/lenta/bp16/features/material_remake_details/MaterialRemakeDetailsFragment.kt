package com.lenta.bp16.features.material_remake_details

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentMaterialRemakeDetailsBinding
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.ui.OrderByBarcode
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class MaterialRemakeDetailsFragment : CoreFragment<FragmentMaterialRemakeDetailsBinding, MaterialRemakeDetailsViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener, OnScanResultListener {

    override fun getLayoutId(): Int {
        return R.layout.fragment_material_remake_details
    }

    override fun getPageNumber(): String {
        return SCREEN_NUMBER
    }

    private val materialIngredientDataInfo: MaterialIngredientDataInfo by unsafeLazy {
        arguments?.getParcelable<MaterialIngredientDataInfo>(KEY_INGREDIENT)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_INGREDIENT")
    }

    // name of parent component
    private val parentName: String by unsafeLazy {
        arguments?.getString(KEY_PARENT_NAME)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_PARENT_NAME")
    }

    private val parentCode: String by unsafeLazy {
        arguments?.getString(KEY_PARENT_CODE)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_PARENT_CODE")
    }

    private val eanInfo: OrderByBarcode by unsafeLazy {
        arguments?.getParcelable<OrderByBarcode>(KEY_EAN_INFO)
                ?: throw IllegalArgumentException("There is no argument value with key ${KEY_EAN_INFO}")
    }

    init {
        lifecycleScope.launchWhenResumed {
            vm.requestFocusToCount.value = true
        }
    }

    override fun getViewModel(): MaterialRemakeDetailsViewModel {
        provideViewModel(MaterialRemakeDetailsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.materialIngredient.value = materialIngredientDataInfo
            it.eanInfo.value = eanInfo
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
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.orders)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.getWeight)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickOrders()
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

    companion object {
        private const val SCREEN_NUMBER = "16/83"
        private const val KEY_INGREDIENT = "KEY_INGREDIENT"
        private const val KEY_PARENT_CODE = "KEY_PARENT_CODE"
        private const val KEY_PARENT_NAME = "KEY_PARENT_NAME"
        private const val KEY_EAN_INFO = "KEY_EAN_INFO"

        fun newInstance(selectedIngredient: MaterialIngredientDataInfo, parentCode: String, parentName: String, eanInfo: OrderByBarcode): MaterialRemakeDetailsFragment {
            return MaterialRemakeDetailsFragment().apply {
                arguments = bundleOf(
                        KEY_INGREDIENT to selectedIngredient,
                        KEY_PARENT_CODE to parentCode,
                        KEY_PARENT_NAME to parentName,
                        KEY_EAN_INFO to eanInfo
                )
            }
        }
    }
}