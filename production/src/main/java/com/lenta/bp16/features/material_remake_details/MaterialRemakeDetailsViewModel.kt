package com.lenta.bp16.features.material_remake_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.data.IScales
import com.lenta.bp16.features.ingredient_details.IngredientDetailsViewModel
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.params.IngredientDataCompleteParams
import com.lenta.bp16.model.ingredients.ui.OrderByBarcode
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.CompleteIngredientByMaterialNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject
import kotlin.properties.Delegates

class MaterialRemakeDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resourceManager: IResourceManager

    @Inject
    lateinit var scales: IScales

    @Inject
    lateinit var completePackMaterialNetRequest: CompleteIngredientByMaterialNetRequest

    // значение параметра OBJ_CODE из родительского компонента заказа
    var parentCode: String by Delegates.notNull()

    // выбранный ингредиент
    val materialIngredient by unsafeLazy {
        MutableLiveData<MaterialIngredientDataInfo>()
    }

    //Список параметров EAN для ингредиента
    val eanInfo by unsafeLazy {
        MutableLiveData<OrderByBarcode>()
    }

    // Комплектация
    val weightField: MutableLiveData<String> = MutableLiveData(DEFAULT_WEIGHT)

    // суффикс
    val suffix: String by unsafeLazy {
        resourceManager.kgSuffix()
    }

    /**Для проверки весового ШК*/
    private val weightValue = listOf(VALUE_23, VALUE_24, VALUE_27, VALUE_28)

    val ean = MutableLiveData("")

    // Focus by request
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)

    private val entered = weightField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    private val weighted = MutableLiveData(0.0)

    private val total = entered.map {
        it.sumWith(weighted.value ?: 0.0)
    }

    val totalWithUnits = total.map {
        "${it.dropZeros()} ${resourceManager.kgSuffix()}"
    }

    val planQntWithSuffix by unsafeLazy {
        materialIngredient.combineLatest(eanInfo).map {
            val uom: String? =
                    when (eanInfo.value?.ean_nom.orEmpty()) {
                        OrderByBarcode.KAR -> Uom.KAR.name
                        OrderByBarcode.ST -> Uom.ST.name
                        else -> Uom.KG.name
                    }
            MutableLiveData("${materialIngredient.value?.plan_qnt} $uom")
        }
    }

    fun onCompleteClicked() = launchUITryCatch {
        val weight = total.value ?: 0.0
        if (weight == 0.0) {
            navigator.showAlertWeightNotSet()
        } else {
            navigator.showProgressLoadingData()
            val result = completePackMaterialNetRequest(
                    params = IngredientDataCompleteParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = resourceManager.deviceIp,
                            parent = parentCode,
                            matnr = parentCode,
                            fact = weight,
                            mode = IngredientDataCompleteParams.MODE_MATERIAL,
                            personnelNumber = sessionInfo.personnelNumber.orEmpty()
                    )
            )
            result.also {
                navigator.hideProgress()
            }.either(::handleFailure) {
                navigator.goBack()
            }
        }
    }

    fun onScanResult(data: String) {
        ean.value = data
        preparationEanForSearch()
    }

    private fun preparationEanForSearch() {
        var barcode = ean.value.orEmpty()
        if (weightValue.contains(barcode.substring(0 until 2))) {
            barcode = barcode.replace(barcode.takeLast(6), "000000")
        }
        setWeight(barcode)
    }

    private fun setWeight(barcode: String) {
        val ean = eanInfo.value?.ean
        if (ean == barcode) {
            val umrez = eanInfo.value?.ean_umrez?.toDouble() //Числитель
            val umren = eanInfo.value?.ean_umren?.toDouble() //Знаменатель
            val result = umrez?.div(umren ?: 0.0)
            weighted.value = result
        } else {
            weighted.value = barcode.takeLast(6).take(5).toDouble().div(DIV_TO_KG)
        }
    }

    fun onClickAdd() {
        weighted.value = total.value
        weightField.value = DEFAULT_WEIGHT
        requestFocusToCount.value = true
    }

    fun onClickGetWeight() = launchAsyncTryCatch {
        navigator.showProgressLoadingData()
        scales.getWeight().also {
            navigator.hideProgress()
        }.either(::handleFailure) { weight ->
            weightField.postValue(weight)
        }
    }

    fun onBackPressed() {
        navigator.showNotSavedDataWillBeLost {
            navigator.goBack()
        }
    }

    fun onClickOrders() {
        materialIngredient.value?.let {
            navigator.openTechOrdersScreen(it, parentCode)
        }
    }

    companion object {
        private const val DEFAULT_WEIGHT = "0"

        /**Показатели весового штрихкода*/
        const val VALUE_23 = "23"
        const val VALUE_24 = "24"
        const val VALUE_27 = "27"
        const val VALUE_28 = "28"
        const val DIV_TO_KG = 1000
    }
}