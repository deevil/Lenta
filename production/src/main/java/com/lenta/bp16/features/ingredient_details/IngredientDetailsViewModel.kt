package com.lenta.bp16.features.ingredient_details

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.R
import com.lenta.bp16.data.IScales
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject

class IngredientDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var scales: IScales

    // выбранный ингредиент
    val orderIngredient by unsafeLazy {
        MutableLiveData<OrderIngredientDataInfo>()
    }

    // Комплектация
    val weightField: MutableLiveData<String> = MutableLiveData(DEFAULT_WEIGHT)

    private val entered = weightField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    private val weighted = MutableLiveData(0.0)

    private val total = entered.map {
        it.sumWith(weighted.value ?: 0.0)
    }

    val totalWithUnits = total.map {
        "${it.dropZeros()} ${orderIngredient.value?.buom.orEmpty()}"
    }

    // суффикс
    val suffix: String by unsafeLazy {
        context.getString(R.string.text_weight_hint)
    }

    fun onCompleteClicked() = launchUITryCatch {
        val weight = weightField.value.orEmpty()
        if (weight == DEFAULT_WEIGHT || weight.isEmpty()) {
            navigator.showAlertWeightNotSet()
        } else {
            orderIngredient.value?.let {

            }
        }
    }

    fun onClickAdd() {
        weighted.value = total.value!!
        weightField.value = "0"
    }

    fun onClickGetWeight() = launchAsyncTryCatch {
        navigator.showProgressLoadingData()
        scales.getWeight().also {
            navigator.hideProgress()
        }.either(::handleFailure) { weight ->
            weightField.postValue(weight)
        }
    }

    companion object {
        private const val DEFAULT_WEIGHT = "0"
    }
}