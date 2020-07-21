package com.lenta.bp16.features.ingredient_details

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.R
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class IngredientDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var context: Context

    // Комплектация
    val weightField: MutableLiveData<String> = MutableLiveData(DEFAULT_WEIGHT)

    // выбранный ингредиент
    val orderIngredient by unsafeLazy {
        MutableLiveData<OrderIngredientDataInfo>()
    }

    // суффикс
    val suffix: String by unsafeLazy {
        context.getString(R.string.text_weight_hint)
    }

    fun onCompleteClicked() = launchUITryCatch {
        val weight = weightField.value.orEmpty()
        if(weight == DEFAULT_WEIGHT || weight.isEmpty()) {
            navigator.showAlertWeightNotSet()
        } else {
            orderIngredient.value?.let {

            }
        }
    }

    fun onIngredientClicked() {

    }

    companion object {
        private const val DEFAULT_WEIGHT = "0"
    }
}