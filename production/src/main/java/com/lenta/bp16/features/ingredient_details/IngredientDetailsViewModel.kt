package com.lenta.bp16.features.ingredient_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.data.IScales
import com.lenta.bp16.model.BatchNewDataInfo
import com.lenta.bp16.model.GoodTypeIcon
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ZPartDataInfo
import com.lenta.bp16.model.ingredients.MercuryPartDataInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.params.IngredientDataCompleteParams
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.CompleteIngredientByOrderNetRequest
import com.lenta.bp16.request.ingredients_use_case.get_data.GetMercuryPartDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetProducerDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetZPartDataInfoUseCase
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates

class IngredientDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resourceManager: IResourceManager

    @Inject
    lateinit var scales: IScales

    @Inject
    lateinit var packIngredientsNetRequest: CompleteIngredientByOrderNetRequest

    @Inject
    lateinit var mercuryPartDataInfoUseCase: GetMercuryPartDataInfoUseCase

    @Inject
    lateinit var zPartDataInfoUseCase: GetZPartDataInfoUseCase

    // значение параметра OBJ_CODE из родительского компонента заказа
    var parentCode: String by Delegates.notNull()

    // выбранный ингредиент
    val orderIngredient by unsafeLazy {
        MutableLiveData<OrderIngredientDataInfo>()
    }

    //Список параметров EAN для ингредиента
    val eanInfo by unsafeLazy {
        MutableLiveData<OrderByBarcodeUI>()
    }

    // Комплектация
    val weightField: MutableLiveData<String> = MutableLiveData(DEFAULT_WEIGHT)

    // суффикс
    val suffix: String by unsafeLazy {
        resourceManager.kgSuffix()
    }

    // Focus by request
    val requestFocusToNumberField by unsafeLazy { MutableLiveData(false) }

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

    private val mercuryDataInfo by unsafeLazy {
        MutableLiveData<List<MercuryPartDataInfo>>()
    }

    private val zPartDataInfo by unsafeLazy {
        MutableLiveData<List<ZPartDataInfo>>()
    }

    val producerNameList by unsafeLazy {
        if (!orderIngredient.value?.isVet.isNullOrBlank()) {
            mercuryDataInfo.switchMap {
                asyncLiveData<List<String>> {
                    val producerNameList = it.map { it.prodName.orEmpty() }
                    emit(producerNameList)
                }
            }
        } else {
            zPartDataInfo.switchMap {
                asyncLiveData<List<String>> {
                    val producerNameList = it.map { it.prodName.orEmpty() }
                    emit(producerNameList)
                }
            }
        }
    }
    val selectedProducerPosition = MutableLiveData(0)

    val productionDateField by unsafeLazy {
        if (!orderIngredient.value?.isVet.isNullOrBlank()) {
            mercuryDataInfo.switchMap {
                asyncLiveData<List<String>> {
                    val productionDate = it.map { it.prodDate.orEmpty() }
                    emit(productionDate)
                }
            }
        } else {
            zPartDataInfo.switchMap {
                asyncLiveData<List<String>> {
                    val productionDate = it.map { it.prodDate.orEmpty() }
                    emit(productionDate)
                }
            }
        }
    }

    val selectedDateProductionPosition = MutableLiveData(0)

    /** Определение типа товара */
    val goodTypeIcon by unsafeLazy {
        /*if (!orderIngredient.value?.isVet.isNullOrBlank()) { //Ветеринарный пока никак не должен определяться
        GoodTypeIcon.IS_VET
    } else */if (!orderIngredient.value?.isFact.isNullOrBlank()) {
        GoodTypeIcon.IS_FACT
    } else {
        GoodTypeIcon.IS_PLAN
    }
    }

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        producerNameList.mapSkipNulls {
            val condition = it.isNotEmpty() && (!orderIngredient.value?.isVet.isNullOrBlank() || !orderIngredient.value?.isZpart.isNullOrBlank())
            condition
        }
    }

    /** Условие отображения даты производства */
    val dateVisibleCondition by unsafeLazy {
        !orderIngredient.value?.isVet.isNullOrBlank() || !orderIngredient.value?.isZpart.isNullOrBlank()
    }

    /** Условие отображения кнопки для показа информации о меркурианском товаре*/
    val vetIconInfoCondition by unsafeLazy {
        !orderIngredient.value?.isVet.isNullOrBlank()
    }

    /** Условие активности кнопки добавления партии*/
    val addPartAttributeEnable by unsafeLazy {
        orderIngredient.value?.isVet.isNullOrBlank()
    }

    /** Условие блокировки спиннеров производителя и даты*/
    val disableSpinner by unsafeLazy {
        true //Заменить на условие
    }

    val nextAndAddButtonEnabled: MutableLiveData<Boolean> = producerNameList
            .combineLatest(productionDateField)
            .map {
                val producerName = it?.first
                val productionDate = it?.second
                if (!orderIngredient.value?.isVet.isNullOrBlank()) {
                    !(producerName.isNullOrEmpty() || productionDate.isNullOrEmpty())
                } else {
                    !productionDate.isNullOrEmpty()
                }
            }

    /**Для проверки весового ШК*/
    private val weightValue = listOf(VALUE_23, VALUE_24, VALUE_27, VALUE_28)

    val ean = MutableLiveData("")

    init {
        launchUITryCatch {
            mercuryDataInfo.value = mercuryPartDataInfoUseCase.invoke()
            zPartDataInfo.value = zPartDataInfoUseCase.invoke()
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

    /**
     *
     * Установка веса после сканирования ШК
     *
     * */
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

    fun onCompleteClicked() = launchUITryCatch {
        val weight = total.value ?: 0.0

        val matnr = orderIngredient.value?.matnr.orEmpty()
        var entryId = ""
        var batchId = ""
        var batchNew = emptyList<BatchNewDataInfo>()

        orderIngredient.value?.isVet?.let {
            val selectedIngredient = withContext(Dispatchers.IO) {
                mercuryDataInfo.value?.filter { it.matnr == matnr }
            }
            entryId = selectedIngredient?.getOrNull(0)?.entryId.orEmpty()
        }

        zPartDataInfo.value?.let {
            val selectedZPartData = withContext(Dispatchers.IO) {
                zPartDataInfo.value?.filter { it.matnr == matnr }
            }
            batchId = selectedZPartData?.getOrNull(0)?.batchId.orEmpty()
        }

        if (weight == 0.0) {
            navigator.showAlertWeightNotSet()
        } else {
            orderIngredient.value?.let { ingredient ->
                navigator.showProgressLoadingData()
                val result = packIngredientsNetRequest(
                        params = IngredientDataCompleteParams(
                                tkMarket = sessionInfo.market.orEmpty(),
                                deviceIP = resourceManager.deviceIp,
                                mode = IngredientDataCompleteParams.MODE_INGREDIENT,
                                parent = parentCode,
                                matnr = ingredient.matnr.orEmpty(),
                                fact = weight,
                                personnelNumber = sessionInfo.personnelNumber.orEmpty(),
                                aufnr = matnr,
                                batchId = batchId,
                                batchNew = batchNew,
                                entryId = entryId
                        )
                )
                result.also {
                    navigator.hideProgress()
                }.either(::handleFailure) {
                    navigator.goBack()
                }
            }
        }
    }

    fun onClickAddAttributeButton() {
        navigator.openAddAttributeScreen()
    }

    fun onClickAdd() {
        weighted.value = total.value
        weightField.value = DEFAULT_WEIGHT
        requestFocusToNumberField.value = true
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