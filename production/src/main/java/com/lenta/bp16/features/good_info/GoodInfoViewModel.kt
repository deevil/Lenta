package com.lenta.bp16.features.good_info

import androidx.lifecycle.MutableLiveData
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
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
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

    val weight: MutableLiveData<Double> by unsafeLazy {
        MutableLiveData<Double>()
    }

    val selectedEan = MutableLiveData<String>()

    /**Свойство, указывающее на обязательное заполнение поля "Тара"*/
    private val proFillCond = MutableLiveData<String>()

    /**Свойство, указывающее на отображение поля "Тара"*/
    val proIncludeCond = MutableLiveData<Boolean>()
    val zPartFlag = MutableLiveData<Boolean>()

    /**Количество*/
    val quantityField = MutableLiveData("")
    val requestFocusQuantityField = MutableLiveData(true)

    private val dateInfoSpinner = mutableListOf(PROD_DATE, SELF_LIFE)

    /**Производитель*/
    private val producerList: MutableLiveData<ProducerUI> = MutableLiveData()
    val producerNameField: MutableLiveData<List<String>> = MutableLiveData()
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

    val enabledCompleteButton: MutableLiveData<Boolean> = quantityField
            .combineLatest(warehouseSender)
            .combineLatest(warehouseReceiver)
            .combineLatest(containerField)
            .map {
                val fillQuantity = it?.first?.first?.first
                val fillWarehouseSender = it?.first?.first?.second
                val fillWarehouseReceiver = it?.first?.second
                val fillContainerField = it?.second
                if (!proFillCond.value.isNullOrBlank())
                    !(fillQuantity.isNullOrBlank() || fillWarehouseReceiver.isNullOrEmpty() || fillWarehouseSender.isNullOrEmpty() || fillContainerField.isNullOrEmpty())
                else
                    !(fillQuantity.isNullOrBlank() || fillWarehouseReceiver.isNullOrEmpty() || fillWarehouseSender.isNullOrEmpty())

            }

    var suffix: MutableLiveData<String> = MutableLiveData(Uom.KG.name)

    init {
        setGoodInfo()
        setDateInfo()
        setProducerInfo()
        setStockInfo()
        setContainerInfo()
    }

    private fun setGoodInfo() {
        launchUITryCatch {
            val good = goodParams.value
            weight.value = goodParams.value?.weight
            /**Расчет количества и единиц измерения*/
            val (quantity: Double?, uom: String) =
                    if (weight.value != 0.0) {
                        weight.value?.div(Constants.CONVERT_TO_KG) to Uom.KG.name
                    } else {
                        getPairFromUom(good)
                    }
            quantityField.value = quantity.toString()
            suffix.value = uom
            zPartFlag.value = goodParams.value?.zPart
        }
    }

    private fun getPairFromUom(good: GoodParams?): Pair<Double?, String> {
        return when (good?.uom?.toUom()) {
            Uom.ST -> {
                Constants.QUANTITY_DEFAULT_VALUE_1 to Uom.ST.name
            }
            Uom.KAR -> {
                good.umrez.toInt().div(good.umren.toDouble()) to Uom.KAR.name
            }
            else -> {
                Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.DEFAULT.name
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

    private fun setProducerInfo() {
        producerList.value = goodParams.value?.producers
        producerNameField.value = goodParams.value?.producers?.producerName
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
            val prodCodeSelectedProducer = goodParams.value?.producers?.producerCode?.getOrNull(selectedProducerPosition.value
                    ?: 0).orEmpty()
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
                            buom = suffix.value.orEmpty(),
                            deviceIP = deviceIp.toString(),
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
    }
}