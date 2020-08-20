package com.lenta.bp16.features.order_details

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.R
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class OrderDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var context: Context

    // Комплектация
    val weightField: MutableLiveData<String> = MutableLiveData(DEFAULT_WEIGHT)

    // выбранный ингредиент
    val ingredient by unsafeLazy {
        MutableLiveData<IngredientInfo>()
    }

    //Список параметров EAN для ингредиента
    val eanInfo by unsafeLazy {
        MutableLiveData<OrderByBarcode>()
    }

    // суффикс
    val suffix: String by unsafeLazy {
        context.getString(R.string.text_weight_hint)
    }

    //ingredient.planQntStr

    val planQntWithSuffix by unsafeLazy {
        ingredient.combineLatest(eanInfo).map {
            val uom: String? =
                    when(eanInfo.value?.ean_nom.orEmpty()){
                        OrderByBarcode.KAR -> Uom.KAR.name
                        OrderByBarcode.ST -> Uom.KAR.name
                        else -> Uom.KG.name
                    }
            MutableLiveData("${ingredient.value?.planQnt} $uom")
        }
    }

    val doneQntWithSuffix by unsafeLazy {
        ingredient.combineLatest(eanInfo).map {
            val uom: String =
                    when(eanInfo.value?.ean_nom.orEmpty()){
                        OrderByBarcode.KAR -> Uom.KAR.name
                        OrderByBarcode.ST -> Uom.KAR.name
                        else -> Uom.KG.name
                    }
            MutableLiveData("${ingredient.value?.doneQnt} $uom")
        }
    }

    // Focus by request
    val requestFocusToCount: MutableLiveData<Boolean> by unsafeLazy {
        MutableLiveData(false)
    }

    fun onClickNext() = launchUITryCatch {
        val weight = weightField.value.orEmpty()
        if (weight == DEFAULT_WEIGHT || weight.isEmpty()) {
            navigator.showAlertWeightNotSet()
        } else {
            ingredient.value?.let {
                navigator.openOrderIngredientsListScreen(
                        weight = weight,
                        selectedIngredient = it
                )
            }
        }
    }

    companion object {
        private const val DEFAULT_WEIGHT = "0"
    }

}