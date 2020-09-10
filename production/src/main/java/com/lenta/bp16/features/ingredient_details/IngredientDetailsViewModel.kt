package com.lenta.bp16.features.ingredient_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.data.IScales
import com.lenta.bp16.model.*
import com.lenta.bp16.model.ingredients.MercuryPartDataInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.params.IngredientDataCompleteParams
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.CompleteIngredientByOrderNetRequest
import com.lenta.bp16.request.ingredients_use_case.get_data.GetAddAttributeInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetMercuryPartDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetWarehouseForSelectedItemUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetZPartDataInfoUseCase
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
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

    @Inject
    lateinit var addAttributeInfoUseCase: GetAddAttributeInfoUseCase

    @Inject
    lateinit var warehouseForSelectedItemUseCase: GetWarehouseForSelectedItemUseCase

    // значение параметра OBJ_CODE из родительского компонента заказа
    var parentCode: String by Delegates.notNull()

    // выбранный ингредиент
    val orderIngredient = MutableLiveData<OrderIngredientDataInfo>()

    //Список параметров EAN для ингредиента
    val eanInfo = MutableLiveData<OrderByBarcodeUI>()

    private val mercuryDataInfo = MutableLiveData<List<MercuryPartDataInfo>>()
    private val zPartDataInfo = MutableLiveData<List<ZPartDataInfo>>()
    private val addedAttribute = MutableLiveData<List<AddAttributeProdInfo>>()
    private val warehouseSelected = MutableLiveData<List<String>>()

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

    val producerNameField by unsafeLazy {
        MutableLiveData<List<String>>()
    }


    val selectedProducerPosition = MutableLiveData(0)

    val productionDateField by unsafeLazy {
        MutableLiveData<List<String>>()
    }

    val selectedDateProductionPosition = MutableLiveData(0)

    /** Определение типа товара */
    val goodTypeIcon by unsafeLazy {
        /*if (!orderIngredient.value?.isVet.isNullOrBlank()) { //Ветеринарный пока никак не должен определяться
        GoodTypeIcon.IS_VET
    } else */if (!orderIngredient.value?.isFact.isNullOrBlank()) {
        GoodTypeIcon.FACT
    } else {
        GoodTypeIcon.PLAN
    }
    }

    /** Условие отображения ошибки, если лист производителей заполнен с пробелами */
    private val alertNotFoundProducerName = MutableLiveData<Boolean>()

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        val isVet = !orderIngredient.value?.isVet.isNullOrBlank()
        val isZPart = !orderIngredient.value?.isZpart.isNullOrBlank()
        val condition = when {
            isVet -> true
            !isVet && isZPart -> checkZPartProducerVisibleCondition().first
            else -> false
        }
        alertNotFoundProducerName.value = checkZPartProducerVisibleCondition().second
        condition
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

    val nextAndAddButtonEnabled: MutableLiveData<Boolean> = producerNameField
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

    private fun checkZPartProducerVisibleCondition(): Pair<Boolean, Boolean> {

        val producerVisibleCondition = zPartDataInfo.switchMap {
            asyncLiveData<List<String>> {
                val zPartProducerNameList = it.map { it.prodName.orEmpty() }
                emit(zPartProducerNameList)
            }
        }

        val producersList = producerVisibleCondition.value.orEmpty()

        var fullItemCount = 0
        for (zPartName in producersList) {
            if (zPartName.isNotEmpty()) {
                fullItemCount++ //Считаем количество не пустых полей в списке
            }
        }

        val visibleStatus = when {
            (fullItemCount == 0) -> ProducerDataStatus.GONE
            (fullItemCount == producersList.size) -> ProducerDataStatus.VISIBLE
            else -> ProducerDataStatus.ALERT
        }

        return when (visibleStatus) {
            ProducerDataStatus.GONE -> false to false
            ProducerDataStatus.VISIBLE -> true to false
            ProducerDataStatus.ALERT -> true to true
        }
    }

    fun updateData() {
        launchUITryCatch {
            mercuryDataInfo.value = mercuryPartDataInfoUseCase()
            zPartDataInfo.value = zPartDataInfoUseCase()
            addedAttribute.value = addAttributeInfoUseCase()
            warehouseSelected.value = warehouseForSelectedItemUseCase()
            if (alertNotFoundProducerName.value == true) {
                navigator.goBack()
                navigator.showAlertProducerCodeNotFound()
            } else {
                checkProducerInfo()
                checkDataInfo()
            }
        }
    }

    private fun checkProducerInfo() {
        /** Если был передан производитель из AddAttributeFragment, то заполнять данными из нее*/
        if (!addedAttribute.value.isNullOrEmpty()) {
            addedAttribute.value?.let { addAttributeDataInfoList ->
                val producerNameList = addAttributeDataInfoList.map { it.name }
                producerNameField.value = producerNameList
            }
        } else {
            if (!orderIngredient.value?.isVet.isNullOrBlank()) {
                mercuryDataInfo.value?.let { mercuryDataInfoList ->
                    val producerNameList = mercuryDataInfoList.map { it.prodName.orEmpty() }.toMutableList()
                    if (producerNameList.size > 1) {
                        producerNameList.add(0, Constants.CHOOSE_PRODUCER)
                    }
                    producerNameField.value = producerNameList
                }
            } else {
                zPartDataInfo.value?.let { zpartDataInfoList ->
                    val producerNameList = zpartDataInfoList.map { it.prodName.orEmpty() }.toMutableList()
                    if (producerNameList.size > 1) {
                        producerNameList.add(0, Constants.CHOOSE_PRODUCER)
                    }
                    producerNameField.value = producerNameList
                }
            }
        }
    }

    private fun checkDataInfo() {
        /** Если была передана дата из AddAttributeFragment, то заполнять данными из нее*/
        if (!addedAttribute.value.isNullOrEmpty()) {
            addedAttribute.value?.let {
                val productionDate = it.map { it.date }
                productionDateField.value = productionDate
            }
        } else {
            if (!orderIngredient.value?.isVet.isNullOrBlank()) {
                mercuryDataInfo.value?.let {
                    val productionDate = it.map { it.prodDate.orEmpty() }.toMutableList()
                    if (productionDate.size > 1) {
                        productionDate.add(0, Constants.CHOOSE_PRODUCTION_DATE)
                    }
                    productionDateField.value = productionDate
                }
            } else {
                zPartDataInfo.value?.let {
                    val productionDate = it.map { it.prodDate.orEmpty() }.toMutableList()
                    if (productionDate.size > 1) {
                        productionDate.add(0, Constants.CHOOSE_PRODUCTION_DATE)
                    }
                    productionDateField.value = productionDate
                }
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

    /**
     *
     * Установка веса после сканирования ШК
     *
     * */
    private fun setWeight(barcode: String) {
        eanInfo.value?.let { eanInfoValue ->
            val ean = eanInfoValue.ean
            if (ean == barcode) {
                val umrez = eanInfoValue.ean_umrez.toDouble() //Числитель
                val umren = eanInfoValue.ean_umren.toDouble() //Знаменатель
                val result = umrez.div(umren)
                weighted.value = result
            } else {
                weighted.value = barcode.takeLast(6).take(5).toDouble().div(DIV_TO_KG)
            }
        }
    }


    fun onCompleteClicked() = launchUITryCatch {
        val weight = total.value ?: 0.0

        val matnr = orderIngredient.value?.matnr.orEmpty()
        var entryId = ""
        var batchId = ""
        var batchNew = emptyList<BatchNewDataInfoParam>()

        orderIngredient.value?.isVet?.let {
            val selectedIngredient = withContext(Dispatchers.IO) {
                mercuryDataInfo.value?.filter { it.matnr == matnr }
            }
            entryId = selectedIngredient?.getOrNull(0)?.entryId.orEmpty()
        }

        zPartDataInfo.value?.let {
            withContext(Dispatchers.IO) {
                val zPartInfo = zPartDataInfo.value?.filter { it.matnr == matnr }?.let {
                    batchId = it.getOrNull(0)?.batchId.orEmpty()
                }
                /** Если не удалось определить партию*/
                if (zPartInfo == null) {
                    val addedAttributeInfo = addedAttribute.value?.getOrNull(0)
                    val selectedWarehouse = warehouseSelected.value?.getOrNull(0).orEmpty()
                    addedAttributeInfo?.let {
                        batchNew = listOf(BatchNewDataInfoParam(
                                prodCode = addedAttributeInfo.code,
                                prodDate = addedAttributeInfo.date,
                                prodTime = addedAttributeInfo.time,
                                lgort = selectedWarehouse
                        ))
                    }
                }
            }
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
                                batchNewParam = batchNew,
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
        val orderIngredient = orderIngredient.value
        orderIngredient?.let {
            val material = it.getFormattedMaterial()
            val name = it.name.orEmpty()
            val shelfLife = it.shelfLife.orEmpty()
            navigator.openAddAttributeScreen(material, name, parentCode, shelfLife)
        }.orIfNull {
            navigator.showOrderIngredientErrorScreen()
        }
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