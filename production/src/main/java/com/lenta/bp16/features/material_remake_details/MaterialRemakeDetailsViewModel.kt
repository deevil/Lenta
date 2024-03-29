package com.lenta.bp16.features.material_remake_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.data.IScales
import com.lenta.bp16.features.ingredient_details.IngredientDetailsViewModel
import com.lenta.bp16.model.AddAttributeProdInfo
import com.lenta.bp16.model.BatchNewDataInfoParam
import com.lenta.bp16.model.GoodTypeIcon
import com.lenta.bp16.model.IDataInfo
import com.lenta.bp16.model.ingredients.OrderByBarcode.Companion.KAR
import com.lenta.bp16.model.ingredients.OrderByBarcode.Companion.KOR_RUS
import com.lenta.bp16.model.ingredients.OrderByBarcode.Companion.ST
import com.lenta.bp16.model.ingredients.OrderByBarcode.Companion.ST_RUS
import com.lenta.bp16.model.ingredients.params.IngredientDataCompleteParams
import com.lenta.bp16.model.ingredients.params.MaterialDataCompleteParams
import com.lenta.bp16.model.ingredients.ui.MaterialIngredientDataInfoUI
import com.lenta.bp16.model.ingredients.ui.MercuryPartDataInfoUI
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.model.ingredients.ui.ZPartDataInfoUI
import com.lenta.bp16.model.managers.IAttributeManager
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.Constants.CHOOSE_ITEM
import com.lenta.bp16.platform.base.IZpartVisibleConditions
import com.lenta.bp16.platform.extention.distinctAndAddFirstValue
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.CompleteIngredientByMaterialNetRequest
import com.lenta.bp16.request.ingredients_use_case.get_data.GetMercuryPartDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetWarehouseForSelectedItemUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetZPartDataInfoUseCase
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_dd_mm_yyyy
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_yyyy_mm_dd
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.getFormattedDate
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates

class MaterialRemakeDetailsViewModel : CoreViewModel(), IZpartVisibleConditions {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resourceManager: IResourceManager

    @Inject
    lateinit var scales: IScales

    @Inject
    lateinit var attributeManager: IAttributeManager

    @Inject
    lateinit var completePackMaterialNetRequest: CompleteIngredientByMaterialNetRequest

    @Inject
    lateinit var getMercuryPartDataInfoUseCase: GetMercuryPartDataInfoUseCase

    @Inject
    lateinit var getZPartDataInfoUseCase: GetZPartDataInfoUseCase

    @Inject
    lateinit var warehouseForSelectedItemUseCase: GetWarehouseForSelectedItemUseCase

    // значение параметра OBJ_CODE из родительского компонента заказа
    var parentCode: String by Delegates.notNull()

    // выбранный ингредиент
    val materialIngredient = MutableLiveData<MaterialIngredientDataInfoUI>()

    //Список параметров EAN для ингредиента
    val eanInfo = MutableLiveData<OrderByBarcodeUI>()

    private val mercuryDataInfos = MutableLiveData<List<MercuryPartDataInfoUI>>()
    override val zPartDataInfos = MutableLiveData<List<ZPartDataInfoUI>>()
    private val addedAttribute = MutableLiveData<AddAttributeProdInfo>()
    private val warehouseSelected = MutableLiveData<List<String>>()

    // Комплектация
    val weightField: MutableLiveData<String> = MutableLiveData(DEFAULT_WEIGHT)

    // суффикс
    val suffix = eanInfo.map {
        val uom: String =
                when (eanInfo.value?.ean_nom.orEmpty()) {
                    KAR, KOR_RUS -> Uom.KAR.name
                    ST, ST_RUS -> Uom.ST.name
                    else -> Uom.KG.name
                }
        uom
    }

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
        "${it.dropZeros()} ${suffix.value}"
    }

    val planQntWithSuffix by unsafeLazy {
        materialIngredient.mapSkipNulls {
            suffix.mapSkipNulls {
                "${materialIngredient.value?.plan_qnt?.toDouble().dropZeros()} ${suffix.value}"
            }
        }
    }

    val doneQtnWithSuffix by unsafeLazy {
        materialIngredient.mapSkipNulls {
            suffix.mapSkipNulls {
                "${materialIngredient.value?.done_qnt?.toDouble().dropZeros()} ${suffix.value}"
            }
        }
    }

    private val producerInfos by unsafeLazy {
        MutableLiveData<List<ProducerInfo>>()
    }

    val producerNames: MutableLiveData<List<String>> by unsafeLazy {
        producerInfos.mapSkipNulls { names ->
            names.mapNotNull {
                it.name
            }
        }
    }

    val selectedProducerPosition = MutableLiveData(0)

    val productionDates by unsafeLazy {
        productionUnformattedDates.mapSkipNulls { unformattedDates ->
            unformattedDates.map {
                try {
                    getFormattedDate(it, DATE_FORMAT_yyyy_mm_dd, DATE_FORMAT_dd_mm_yyyy)
                } catch (e: Exception) {
                    CHOOSE_ITEM
                }
            }
        }
    }

    private val productionUnformattedDates by unsafeLazy {
        MutableLiveData<List<String>>()
    }

    val selectedDateProductionPosition = MutableLiveData(0)

    /** Определение типа товара */
    val goodTypeIcon by unsafeLazy {
        materialIngredient.mapSkipNulls {
            getGoodTypeIcon(it)
        }
    }

    /** Условие отображения ошибки, если лист производителей заполнен с пробелами */
    private val alertNotFoundProducerName = MutableLiveData<Boolean>()

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        materialIngredient.mapSkipNulls { ingredient ->
            producerConditions.mapSkipNulls { cond ->
                val isVet = ingredient.isVet.isSapTrue()
                val isZPart = ingredient.isZpart.isSapTrue()
                val condition = when {
                    isVet -> true
                    !isVet && isZPart -> cond.first
                    else -> false
                }
                alertNotFoundProducerName.value = cond.second
                condition
            }
        }
    }

    /** Условие отображения даты производства */
    val dateVisibleCondition = materialIngredient.mapSkipNulls {
        it.isVet.isSapTrue() || it.isZpart.isSapTrue()
    }

    /** Условие отображения кнопки для показа информации о меркурианском товаре*/
    val vetIconInfoCondition = materialIngredient.mapSkipNulls {
        it.isVet.isSapTrue()
    }

    /** Условие активности кнопки добавления партии*/
    val addPartAttributeEnable = materialIngredient.mapSkipNulls {
        !it.isVet.isSapTrue()
    }

    /** Условие блокировки спиннеров производителя и даты*/
    val disableSpinner = MutableLiveData<Boolean>()

    /** Условие разблокировки кнопок добавить и завершить */
    val nextAndAddButtonEnabled: MutableLiveData<Boolean> = producerInfos
            .combineLatest(productionDates)
            .mapSkipNulls {
                val (producerName, productionDate) = it
                if (materialIngredient.value?.isVet.isSapTrue()) {
                    !(producerName.isNullOrEmpty() || productionDate.isNullOrEmpty())
                } else {
                    !productionDate.isNullOrEmpty()
                }
            }

    /** Для проверки весового ШК */
    private val weightValue = listOf(VALUE_23, VALUE_24, VALUE_27, VALUE_28)

    val ean = MutableLiveData("")

    private fun getGoodTypeIcon(materialIngredientDataInfo: MaterialIngredientDataInfoUI): GoodTypeIcon {
        return when {
            materialIngredientDataInfo.isVet.isSapTrue() -> GoodTypeIcon.VET
            materialIngredientDataInfo.isFact.isSapTrue() -> GoodTypeIcon.FACT
            else -> GoodTypeIcon.PLAN
        }
    }

    fun chooseGoodInfoScreen() {
        when (goodTypeIcon.value) {
            GoodTypeIcon.VET -> navigator.openVetInfoScreen()
            GoodTypeIcon.FACT -> navigator.openFactInfoScreen()
            else -> navigator.openPlanInfoScreen()
        }
    }

    fun updateData() {
        launchUITryCatch {
            mercuryDataInfos.value = getMercuryPartDataInfoUseCase()
            zPartDataInfos.value = getZPartDataInfoUseCase()
            addedAttribute.value = attributeManager.currentAttribute.value
            warehouseSelected.value = warehouseForSelectedItemUseCase()
            if (alertNotFoundProducerName.value == true) {
                navigator.goBack()
                navigator.showAlertProducerCodeNotFound()
            } else {
                disableSpinner.value = addedAttribute.value?.let { false }.orIfNull { true }
                checkProduceAndDataInfo()
            }
        }
    }

    private fun checkProduceAndDataInfo() {
        /** Если был передан производитель из AddAttributeFragment, то заполнять данными из нее*/
        val resultProducers = getProducers().toMutableList()
        if (resultProducers.size > 1) {
            resultProducers.add(0, ProducerInfo(name = CHOOSE_ITEM))
        }
        producerInfos.value = resultProducers

        val resultDates = getUnformattedDates().toMutableList()
        if (resultDates.size > 1) {
            resultDates.add(0, Constants.CHOOSE_ITEM)
        }
        productionUnformattedDates.value = resultDates
    }

    private fun getProducers(): List<ProducerInfo> {
        val addedAttributeIsNotEmpty = !addedAttribute.value?.name.isNullOrBlank()
        val orderIngredientIsVet = materialIngredient.value?.isVet.isSapTrue()
        return if (addedAttributeIsNotEmpty) {
            addedAttribute.value?.run { listOf(ProducerInfo(code, name)) }.orEmpty()
        } else {
            val producerToDistinct = getLiveDatesToDistinct(orderIngredientIsVet)
            producerToDistinct?.distinctAndAddFirstValue({ it.prodCode to it.prodName }, { ProducerInfo(it.prodCode, it.prodName) }).orEmpty()
        }
    }

    private fun getUnformattedDates(): List<String> {
        val addedAttributeIsNotEmpty = !addedAttribute.value?.name.isNullOrBlank()
        val orderIngredientIsVet = materialIngredient.value?.isVet.isSapTrue()
        return if (addedAttributeIsNotEmpty) {
            addedAttribute.value?.run { listOf(date) }.orEmpty()
        } else {
            val datesToDistinct = getLiveDatesToDistinct(orderIngredientIsVet)
            datesToDistinct?.distinctAndAddFirstValue({ it.prodDate }, { it.prodDate }).orEmpty()
        }
    }

    private fun getLiveDatesToDistinct(isOrderIngredientIsVet: Boolean): List<IDataInfo>? {
        return if (isOrderIngredientIsVet) {
            mercuryDataInfos.value
        } else {
            zPartDataInfos.value
        }
    }

    private fun getEntryId(): String {
        return materialIngredient.value?.isVet?.let {
            mercuryDataInfos.value?.getOrNull(0)?.entryId.orEmpty()
        }.orEmpty()
    }

    private suspend fun getZPartInfo(prodCode: String, prodDate: String): ZPartDataInfoUI? = withContext(Dispatchers.IO) {
        zPartDataInfos.value?.findLast { it.prodCode == prodCode && it.prodDate == prodDate }
    }

    private suspend fun setBatchNewInfo(): List<BatchNewDataInfoParam>? {
        return withContext(Dispatchers.IO) {
            /** Если не удалось определить партию*/
            val addedAttributeInfo = addedAttribute.value
            val selectedWarehouse = warehouseSelected.value?.getOrNull(0).orEmpty()
            addedAttributeInfo?.let {
                val prodDate = getFormattedDate(addedAttributeInfo.date, DATE_FORMAT_dd_mm_yyyy, DATE_FORMAT_yyyy_mm_dd)
                listOf(BatchNewDataInfoParam(
                        prodCode = addedAttributeInfo.code,
                        prodDate = prodDate,
                        prodTime = addedAttributeInfo.time,
                        lgort = selectedWarehouse
                ))
            }
        }
    }

    fun onCompleteClicked() = launchUITryCatch {
        val weight = total.value ?: 0.0
        val ktsch = materialIngredient.value?.ktsch.orEmpty()

        val producerIndex = selectedProducerPosition.getOrDefault()
        val productionDateIndex = selectedDateProductionPosition.getOrDefault()
        val prodCode = producerInfos.value?.getOrNull(producerIndex)?.code.orEmpty()
        val prodDate = productionUnformattedDates.getOrEmpty(productionDateIndex)

        val entryId = getEntryId()
        val zPartInfo = getZPartInfo(prodCode, prodDate)
        val batchId = zPartInfo?.batchId.orEmpty()

        val batchNew = if (zPartInfo == null) {
            setBatchNewInfo()
        } else {
            emptyList()
        }

        if (weight == 0.0) {
            navigator.showAlertWeightNotSet()
        } else {
            navigator.showProgressLoadingData()
            val result = completePackMaterialNetRequest(
                    params = MaterialDataCompleteParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = resourceManager.deviceIp,
                            ktsch = ktsch,
                            parent = parentCode,
                            matnr = parentCode,
                            fact = weight,
                            mode = IngredientDataCompleteParams.MODE_MATERIAL,
                            personnelNumber = sessionInfo.personnelNumber.orEmpty(),
                            batchId = batchId,
                            batchNewParam = batchNew.orEmpty(),
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

    fun onClickAddAttributeButton() {
        val materialIngredient = materialIngredient.value
        materialIngredient?.let {
            navigator.openMaterialAttributeScreen(materialIngredient, parentCode)
        }.orIfNull {
            navigator.showOrderIngredientErrorScreen()
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
        attributeManager.currentAttribute.value = null
        navigator.showNotSavedDataWillBeLost {
            navigator.goBack()
        }
    }

    fun onClickOrders() {
        materialIngredient.value?.let {
            val materialIngredientKtsch = materialIngredient.value?.ktsch.orEmpty()
            navigator.openTechOrdersScreen(it, parentCode, materialIngredientKtsch)
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

    internal data class ProducerInfo(val code: String? = null, val name: String? = null) {
        val isEmpty: Boolean
            get() = code.isNullOrEmpty() && name.isNullOrEmpty()
    }
}