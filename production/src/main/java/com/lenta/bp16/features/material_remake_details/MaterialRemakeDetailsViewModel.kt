package com.lenta.bp16.features.material_remake_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.data.IScales
import com.lenta.bp16.model.GoodTypeIcon
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ZPartDataInfo
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.MercuryPartDataInfo
import com.lenta.bp16.model.ingredients.params.IngredientDataCompleteParams
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.CompleteIngredientByMaterialNetRequest
import com.lenta.bp16.request.ingredients_use_case.get_data.GetMercuryPartDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetProducerDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetZPartDataInfoUseCase
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
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

    @Inject
    lateinit var mercuryPartDataInfoUseCase: GetMercuryPartDataInfoUseCase

    @Inject
    lateinit var zPartDataInfoUseCase: GetZPartDataInfoUseCase

    // значение параметра OBJ_CODE из родительского компонента заказа
    var parentCode: String by Delegates.notNull()

    // выбранный ингредиент
    val materialIngredient by unsafeLazy {
        MutableLiveData<MaterialIngredientDataInfo>()
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

    private val mercuryDataInfo by unsafeLazy {
        MutableLiveData<List<MercuryPartDataInfo>>()
    }

    private val zPartDataInfo by unsafeLazy {
        MutableLiveData<List<ZPartDataInfo>>()
    }

    val producerNameList by unsafeLazy {
        if (!materialIngredient.value?.isVet.isNullOrBlank()) {
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
        if (!materialIngredient.value?.isVet.isNullOrBlank()) {
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
        /*if (!materialIngredient.value?.isVet.isNullOrBlank()) { //Ветеринарный пока никак не должен определяться
        GoodTypeIcon.IS_VET
    } else */if (!materialIngredient.value?.isFact.isNullOrBlank()) {
        GoodTypeIcon.IS_FACT
    } else {
        GoodTypeIcon.IS_PLAN
    }
    }

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        producerNameList.mapSkipNulls {
            val condition = it.isNotEmpty() && (!materialIngredient.value?.isVet.isNullOrBlank() || !materialIngredient.value?.isZpart.isNullOrBlank())
            condition
        }
    }

    /** Условие отображения даты производства */
    val dateVisibleCondition by unsafeLazy {
        !materialIngredient.value?.isVet.isNullOrBlank() || !materialIngredient.value?.isZpart.isNullOrBlank()
    }

    /** Условие отображения кнопки для показа информации о меркурианском товаре*/
    val vetIconInfoCondition by unsafeLazy {
        !materialIngredient.value?.isVet.isNullOrBlank()
    }

    /** Условие активности кнопки добавления партии*/
    val addPartAttributeEnable by unsafeLazy {
        materialIngredient.value?.isVet.isNullOrBlank()
    }

    /** Условие блокировки спиннеров производителя и даты*/
    val disableSpinner by unsafeLazy {
        true //Заменить на условие
    }

    /** Условие разблокировки кнопок добавить и завершить */
    val nextAndAddButtonEnabled: MutableLiveData<Boolean> = producerNameList
            .combineLatest(productionDateField)
            .map {
                val producerName = it?.first
                val productionDate = it?.second
                if (!materialIngredient.value?.isVet.isNullOrBlank()) {
                    !(producerName.isNullOrEmpty() || productionDate.isNullOrEmpty())
                } else {
                    !productionDate.isNullOrEmpty()
                }
            }

    /** Для проверки весового ШК */
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

    init {
        launchUITryCatch {
            mercuryDataInfo.value = mercuryPartDataInfoUseCase.invoke()
            zPartDataInfo.value = zPartDataInfoUseCase.invoke()
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
                            personnelNumber = sessionInfo.personnelNumber.orEmpty(),
                            aufnr = "",
                            batchId = "",
                            batchNew = listOf(),
                            entryId = ""
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
        navigator.openAddAttributeScreen()
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
}