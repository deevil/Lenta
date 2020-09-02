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

    val buom: MutableLiveData<Uom> = MutableLiveData()

    private val weightAndUom: LiveData<Pair<Double?, Uom>> = goodParams.switchMap { good ->
        weight.switchMap { weightValue ->
            asyncLiveData<Pair<Double?, Uom>> {
                val pair = weightValue.takeIf { it != 0.0 }
                        ?.run { div(Constants.CONVERT_TO_KG) to Uom.KG }
                        ?: getPairFromUom(good)
                buom.postValue(pair.second)
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
    val selectedProducerPosition = MutableLiveData(0)

    /**Список складов отправителей*/
    val warehouseSender: MutableLiveData<List<String>> = MutableLiveData()
    val selectedWarehouseSenderPosition by unsafeLazy {
        MutableLiveData(appSettings.warehouseSenderPosition)
    }

    /**Список складов получателей*/
    val warehouseReceiver: MutableLiveData<List<String>> = MutableLiveData()
    val selectedWarehouseReceiverPosition by unsafeLazy {
        MutableLiveData(appSettings.warehouseReceiverPosition)
    }

    /**Дата производства и срок годности*/
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
        if (date.isNotEmpty() && date.length == DATE_LENGTH) {
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
        }
    }

    private fun setContainerInfo() = launchUITryCatch {
        val includeCond = database.getIncludeCondition()
        proIncludeCond.value = !includeCond.isNullOrBlank()
        proFillCond.value = database.getProFillCondition()
    }

    private fun saveWarehouses() {
        appSettings.warehouseSenderPosition = selectedWarehouseSenderPosition.value
        appSettings.warehouseReceiverPosition = selectedWarehouseReceiverPosition.value
    }

    /**Проверка даты на корректность*/
    private fun checkDate(): Boolean {
        val checkDate = dateInfoField.value.orEmpty()
        val splitCheckDate = checkDate.split(".")
        val day = splitCheckDate[0].toInt()
        val month = splitCheckDate[1].toInt()
        val year = splitCheckDate[2].toInt()
        val monthWith31Days = listOf(1, 3, 5, 7, 8, 10, 12)
        val monthWith30Days = listOf(4, 6, 9, 11)
        return if (year in 2000..2100 && month in 1..12 && day in 1..31) {
            if (monthWith31Days.contains(month)) {
                day <= 31
            } else if (monthWith30Days.contains(month) && month != 2) {
                day <= 30
            } else if (year % 4 == 0) {
                day <= 29
            } else {
                day <= 28
            }
        } else {
            false
        }
    }

    fun onClickBack() {
        saveWarehouses()
        navigator.openSelectGoodScreen()
    }

    fun onClickComplete() = launchUITryCatch {
        navigator.showProgressLoadingData()
        setDateInfo()
        saveWarehouses()
        val prodCodeSelectedProducer = goodParams.value?.producers?.getOrNull(selectedProducerPosition.value
                ?: 0)?.producerCode.orEmpty()
        val warehouseSenderSelected = warehouseSender.value?.getOrNull(selectedWarehouseSenderPosition.value
                ?: 0).orEmpty()
        val warehouseReceiverSelected = warehouseReceiver.value?.getOrNull(selectedWarehouseReceiverPosition.value
                ?: 0).orEmpty()
        val dateIsCorrect = if (zPartFlag.value == true) {
            checkDate()
        } else {
            true
        }
        if (!dateIsCorrect) {
            navigator.hideProgress()
            navigator.showAlertWrongDate()
        } else {
            val result = movementNetRequest(
                    params = MovementParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            matnr = goodParams.value?.material.orEmpty(),
                            prodCode = prodCodeSelectedProducer,
                            dateProd = producerDate,
                            expirDate = selfLifeDate,
                            lgortExport = warehouseSenderSelected,
                            lgortImport = warehouseReceiverSelected,
                            codeCont = containerField.value.orEmpty(),
                            factQnt = quantityField.value.orEmpty(),
                            buom = buom.value?.code.orEmpty(),
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