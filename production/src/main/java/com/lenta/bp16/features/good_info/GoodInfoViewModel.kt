package com.lenta.bp16.features.good_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.movement.params.MovementParams
import com.lenta.bp16.model.movement.params.WarehouseParams
import com.lenta.bp16.model.pojo.GoodParams
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.bp16.request.MovementNetRequest
import com.lenta.bp16.request.WarehouseNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.toUom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.getFormattedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var movementNetRequest: MovementNetRequest

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var database: DatabaseRepository

    @Inject
    lateinit var warehouseRequest: WarehouseNetRequest

    val deviceIp = MutableLiveData<String>()

    val goodParams = MutableLiveData<GoodParams>()

    val weight: MutableLiveData<Double> = goodParams.mapSkipNulls {
        it?.weight ?: 0.0
    }

    val selectedEan = MutableLiveData<String>()

    /**Свойство, указывающее на обязательное заполнение поля "Тара"*/
    private val proFillCond = MutableLiveData<String>()

    /**Свойство, указывающее на отображение поля "Тара"*/
    val proIncludeCond = MutableLiveData<Boolean>()

    val zPartFlag = goodParams.mapSkipNulls {
        it.zPart
    }

    val requestFocusQuantityField = MutableLiveData(true)

    val buom: MutableLiveData<String> = MutableLiveData("")

    private val weightAndUom: LiveData<Pair<Double?, Uom>> = goodParams.switchMap { good ->
        weight.switchMap { weightValue ->
            asyncLiveData<Pair<Double?, Uom>> {
                val pair = weightValue.takeIf { it != 0.0 }
                        ?.run { div(Constants.CONVERT_TO_KG) to Uom.KG }
                        ?: getPairFromUom(good)
                buom.postValue(pair.second.code)
                emit(pair)
            }
        }
    }

    /**Количество*/
    val quantityField = weightAndUom.mapSkipNulls {
        /**Если товар не весовой, то отбрасываем нули*/
        if (it.second.name != Uom.KG.name) {
            it.first?.dropZeros()
        } else {
            it.first?.toString().orEmpty()
        }
    }

    private val dateInfoSpinner = mutableListOf(PROD_DATE, SELF_LIFE)

    /**Производитель*/
    val producerNameField: LiveData<List<String>> = goodParams.switchMap {
        asyncLiveData<List<String>> {
            val producersNameList = it.producers.map { it.producerName }
            emit(producersNameList)
        }
    }
    val selectedProducerPosition = MutableLiveData(0);

    /**Склад отправитель*/
    val warehouseSender: MutableLiveData<List<String>> = MutableLiveData()
    val warehouseSenderPosition by unsafeLazy {
        appSettings.warehouseSenderPosition
    }
    val selectedWarehouseSenderPosition by unsafeLazy {
        MutableLiveData(warehouseSenderPosition)
    }

    /**Склад получатель*/
    val warehouseReceiver: MutableLiveData<List<String>> = MutableLiveData()
    val warehouseReceiverPosition by unsafeLazy {
        appSettings.warehouseReceiverPosition
    }
    val selectedWarehouseReceiverPosition by unsafeLazy {
        MutableLiveData(warehouseReceiverPosition)
    }

    /**Дата производства и срок годности*/
    private val warehouseSenderSelected by unsafeLazy {
        selectedWarehouseSenderPosition.map {
            warehouseSender.value?.getOrNull(it ?: 0).orEmpty()
        }
    }
    private val warehouseReceiverSelected by unsafeLazy {
        selectedWarehouseReceiverPosition.map {
            warehouseReceiver.value?.getOrNull(it ?: 0).orEmpty()
        }
    }
    val dateInfoField = MutableLiveData("")
    private var producerDate: String = ""
    private var selfLifeDate: String = ""
    val dateInfo: MutableLiveData<List<String>> = MutableLiveData(dateInfoSpinner)
    val selectedDatePosition = MutableLiveData(0)

    val requestFocusDateInfoField = MutableLiveData(true)

    /**Тара*/
    val containerField = MutableLiveData("")

    val enabledCompleteButton: MutableLiveData<Boolean> = dateInfoField
            .combineLatest(quantityField)
            .combineLatest(warehouseSender)
            .combineLatest(warehouseReceiver)
            .combineLatest(containerField)
            .map {
                val fillDate = it?.first?.first?.first?.first
                val fillQuantity = it?.first?.first?.first?.second
                val fillWarehouseSender = it?.first?.first?.second
                val fillWarehouseReceiver = it?.first?.second
                val fillContainerField = it?.second
                if (!proFillCond.value.isNullOrBlank()) {
                    !(fillQuantity.isNullOrBlank() || fillWarehouseReceiver.isNullOrEmpty() || fillWarehouseSender.isNullOrEmpty() || fillContainerField.isNullOrEmpty() || fillDate?.length != DATE_LENGTH)
                } else if (zPartFlag.value == true) {
                    !(fillQuantity.isNullOrBlank() || fillWarehouseReceiver.isNullOrEmpty() || fillWarehouseSender.isNullOrEmpty() || fillDate?.length != DATE_LENGTH)
                } else {
                    !(fillQuantity.isNullOrBlank() || fillWarehouseReceiver.isNullOrEmpty() || fillWarehouseSender.isNullOrEmpty())
                }
            }

    val suffix: LiveData<String> = weightAndUom.mapSkipNulls { it.second.name }

    init {
        setStockInfo()
        setContainerInfo()
    }

    private fun getPairFromUom(good: GoodParams?): Pair<Double?, Uom> {
        return when (good?.uom?.toUom()) {
            Uom.ST -> {
                Constants.QUANTITY_DEFAULT_VALUE_1 to Uom.ST
            }
            Uom.KAR -> {
                good.umrez.toDouble().div(good.umren.toDouble()) to good.buom.toUom()
            }
            else -> {
                Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.KG
            }
        }
    }

    private fun setDateInfo() {
        /**Изменения формата даты только правильном заполнении поля*/
        var date = dateInfoField.value.orEmpty()
        if(date.isNotEmpty() && date.length == DATE_LENGTH){
            date = getFormattedDate(dateInfoField.value.orEmpty(), Constants.DATE_FORMAT_dd_mm_yyyy, Constants.DATE_FORMAT_yyyyMMdd)
        }

        //Заполненной может быть только одна дата
        if (selectedDatePosition.value == 0) {
            producerDate = date
            selfLifeDate = ""
        } else {
            selfLifeDate = date
            producerDate = ""
        }
    }

    private fun setStockInfo() {
        launchUITryCatch {
            warehouseRequest(
                    WarehouseParams(
                            sessionInfo.market.orEmpty()
                    )
            ).either(::handleFailure) { warehouseResult ->
                warehouseSender.value = warehouseResult.warehouseList?.map { it.warehouseName }
                warehouseReceiver.value = warehouseResult.warehouseList?.map { it.warehouseName }
                Unit
            }
            //selectedWarehouseSenderPosition.value = findSelectedWarehouseSender()
            //selectedWarehouseReceiverPosition.value = findSelectedWarehouseReceiver()
        }
    }

    private fun setContainerInfo() = launchUITryCatch {
        val includeCond = database.getIncludeCondition()
        proIncludeCond.value = !includeCond.isNullOrBlank()
        proFillCond.value = database.getProFillCondition()
    }


    /*private suspend fun findSelectedWarehouseSender(): Int = withContext(Dispatchers.IO) {
        var selectedIndex = 0
        val warehouseList = warehouseSender.value
        appSettings.lastWarehouseSender?.let { lastWarehouse ->
            warehouseList?.forEachIndexed { index, warehouse ->
                if (warehouse == lastWarehouse) {
                    warehouseSenderSelected = warehouse
                    selectedIndex = index
                    return@forEachIndexed
                }
            }
        }
        selectedIndex
    }*/

    /*private suspend fun findSelectedWarehouseReceiver(): Int = withContext(Dispatchers.IO) {
        var selectedIndex = 0
        val warehouseList = warehouseReceiver.value
        appSettings.lastWarehouseReceiver?.let { lastWarehouse ->
            warehouseList?.forEachIndexed { index, warehouse ->
                if (warehouse == lastWarehouse) {
                    warehouseReceiverSelected = warehouse
                    selectedIndex = index
                    return@forEachIndexed
                }
            }
        }
        selectedIndex
    }*/

    private fun saveWarehouses() {
        /*warehouseSenderSelected = warehouseSender.value?.getOrNull(selectedWarehouseSenderPosition.value
                ?: 0).orEmpty()
        warehouseReceiverSelected = warehouseReceiver.value?.getOrNull(selectedWarehouseReceiverPosition.value
                ?: 0).orEmpty()
        appSettings.lastWarehouseSender = warehouseSenderSelected
        appSettings.lastWarehouseReceiver = warehouseReceiverSelected*/

        appSettings.warehouseSenderPosition = selectedWarehouseSenderPosition.value
        appSettings.warehouseReceiverPosition = selectedWarehouseReceiverPosition.value
    }

    fun onClickBack() {
        saveWarehouses()
        navigator.openSelectGoodScreen()
    }

    fun onClickComplete() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            setDateInfo()
            saveWarehouses()
            val prodCodeSelectedProducer = goodParams.value?.producers?.getOrNull(selectedProducerPosition.value
                    ?: 0)?.producerCode.orEmpty()
            val result = movementNetRequest(
                    params = MovementParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            matnr = goodParams.value?.material.orEmpty(),
                            prodCode = prodCodeSelectedProducer,
                            dateProd = producerDate,
                            expirDate = selfLifeDate,
                            lgortExport = warehouseSenderSelected.value.orEmpty(),
                            lgortImport = warehouseReceiverSelected.value.orEmpty(),
                            codeCont = "",
                            factQnt = quantityField.value.toString(),
                            buom = buom.value.orEmpty(),
                            deviceIP = deviceIp.value.toString(),
                            personnelNumber = sessionInfo.personnelNumber.orEmpty()
                    )
            )
            result.also {
                navigator.hideProgress()
            }.either(::handleFailure) {
                with(navigator) {
                    showMovingSuccessful {
                        openSelectGoodScreen()
                    }
                }
            }
        }
    }

    companion object {
        const val PROD_DATE = "Дата производства"
        const val SELF_LIFE = "Срок годности"
        const val DATE_LENGTH = 10
    }
}