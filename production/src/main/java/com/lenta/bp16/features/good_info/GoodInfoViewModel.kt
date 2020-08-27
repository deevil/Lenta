package com.lenta.bp16.features.good_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.movement.params.MovementParams
import com.lenta.bp16.model.movement.params.WarehouseParams
import com.lenta.bp16.model.movement.ui.ProducerUI
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
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var movementNetRequest: MovementNetRequest

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

    private val WeightAndUom: LiveData<Pair<Double?, Uom>> = goodParams.switchMap { good ->
        weight.switchMap {
            asyncLiveData<Pair<Double?, Uom>> {
                val pair = weight.value?.takeIf { it != 0.0 }
                        ?.run { div(Constants.CONVERT_TO_KG) to Uom.KG }
                        ?: getPairFromUom(good)
                emit(pair)
            }
        }
    }

    /**Количество*/
    val quantityField = WeightAndUom.mapSkipNulls {
        /**Если товар не весовой, то переводим в целый тип*/
        if (it.second.name != Uom.DATA_KG) {
            it.first?.toInt().toString()
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

    /**Дата производства и срок годности*/
    val dateInfoField = MutableLiveData("")
    private var producerDate: String = ""
    private var selfLifeDate: String = ""
    val dateInfo: MutableLiveData<List<String>> = MutableLiveData(dateInfoSpinner)
    val selectedDatePosition = MutableLiveData(0)

    val requestFocusDateInfoField = MutableLiveData(true)

    /**Склад отправитель*/
    val warehouseSender: MutableLiveData<List<String>> = MutableLiveData()
    val selectedWarehouseSenderPosition = MutableLiveData(0)

    /**Склад получатель*/
    val warehouseReceiver: MutableLiveData<List<String>> = MutableLiveData()
    val selectedWarehouseReceiverPosition = MutableLiveData(0)

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
                } else {
                    !(fillQuantity.isNullOrBlank() || fillWarehouseReceiver.isNullOrEmpty() || fillWarehouseSender.isNullOrEmpty() || fillDate?.length != DATE_LENGTH)
                }
            }

    val suffix: LiveData<String> = WeightAndUom.mapSkipNulls { it.second.name }
    val buom: LiveData<String> = WeightAndUom.mapSkipNulls { it.second.code }

    init {
        setDateInfo()
        setStockInfo()
        setContainerInfo()
    }

    private fun getPairFromUom(good: GoodParams?): Pair<Double?, Uom> {
        return when (good?.uom?.toUom()) {
            Uom.ST -> {
                Constants.QUANTITY_DEFAULT_VALUE_1 to Uom.ST
            }
            Uom.KAR -> {
                good.umrez.toInt().div(good.umren.toDouble()) to Uom.KAR
            }
            else -> {
                Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.DEFAULT
            }
        }
    }

    private fun setDateInfo() {
        launchUITryCatch {
            if (selectedDatePosition.value == 0) {
                producerDate = dateInfoField.value.orEmpty()
            } else {
                selfLifeDate = dateInfoField.value.orEmpty()
            }
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

    private fun setContainerInfo() {
        launchUITryCatch {
            val includeCond = database.getIncludeCondition()
            proIncludeCond.value = !includeCond.isNullOrBlank()
            proFillCond.value = database.getProFillCondition()
        }
    }

    fun onClickComplete() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val prodCodeSelectedProducer = goodParams.value?.producers?.getOrNull(selectedProducerPosition.value
                    ?: 0)?.producerName.orEmpty()
            val warehouseSenderSelected = warehouseSender.value?.getOrNull(selectedWarehouseSenderPosition.value
                    ?: 0).orEmpty()
            val warehouseReceiverSelected = warehouseReceiver.value?.getOrNull(selectedWarehouseReceiverPosition.value
                    ?: 0).orEmpty()
            val result = movementNetRequest(
                    params = MovementParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            matnr = goodParams.value?.material.orEmpty(),
                            prodCode = prodCodeSelectedProducer,
                            dateProd = producerDate,
                            expirDate = selfLifeDate,
                            lgortExport = warehouseSenderSelected,
                            lgortImport = warehouseReceiverSelected,
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
                navigator.openSelectGoodScreen()
            }
        }
    }

    companion object {
        const val PROD_DATE = "Дата производства"
        const val SELF_LIFE = "Срок годности"
        const val DATE_LENGTH = 10
    }
}