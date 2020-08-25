package com.lenta.bp16.features.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.movement.params.WarehouseParams
import com.lenta.bp16.model.movement.result.WarehouseResult
import com.lenta.bp16.model.movement.ui.ProducerUI
import com.lenta.bp16.model.movement.ui.Warehouse
import com.lenta.bp16.model.pojo.GoodParams
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.bp16.repository.IRepoInMemoryHolder
import com.lenta.bp16.request.MovementNetRequest
import com.lenta.bp16.request.MovementParams
import com.lenta.bp16.request.ProductInfoNetRequest
import com.lenta.bp16.request.WarehouseNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.toUom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    /**Количество*/
    val quantityField = MutableLiveData("")
    val requestFocusQuantityField = MutableLiveData(true)

    val warehouseResult: MutableLiveData<WarehouseResult> by unsafeLazy {
        MutableLiveData<WarehouseResult>()
    }

    val dateInfoSpinner = mutableListOf(PROD_DATE, SELF_LIFE)

    /**Производитель*/
    private val producerList: MutableLiveData<ProducerUI> = MutableLiveData()
    val producerNameField: MutableLiveData<List<String>> = MutableLiveData()
    val selectedProducer = MutableLiveData(0);
    val producerClicked = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedProducer.value = position
        }
    }

    /**Дата производства и срок годности*/
    val dateInfoField = MutableLiveData("")
    private var producerDate: String = ""
    private var selfLifeDate: String = ""
    val dateInfo: MutableLiveData<List<String>> = MutableLiveData(dateInfoSpinner)
    val selectedDate = MutableLiveData(0)
    val dateClicked = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedDate.value = position
        }
    }

    val requestFocusDateInfoField = MutableLiveData(true)

    /**Склад отправитель*/
    val warehouseSender: MutableLiveData<List<String>> = MutableLiveData()
    val selectedWarehouseSender = MutableLiveData(0)
    val warehouseSenderClicked = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedWarehouseSender.value = position
        }
    }

    /**Склад получатель*/
    val warehouseReceiver: MutableLiveData<List<String>> = MutableLiveData()
    val selectedWarehouseReceiver = MutableLiveData(0)
    val warehouseReceiverClicked = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedWarehouseReceiver.value = position
        }
    }

    /**Тара*/
    val containerField = MutableLiveData("")

    val enabledCompleteButton = quantityField.map { !it.isNullOrBlank() }

    var suffix: MutableLiveData<String> = MutableLiveData(Uom.KG.name)

    init {
        setProducerInfo()
        setStockInfo()
        setGoodInfo()
        setDateInfo()
        setContainerInfo()
    }

    private fun setGoodInfo() {
        launchUITryCatch {
            weight.value = goodParams.value?.weight
            /**Расчет количества и единиц измерения*/
            val (quantity: Double?, uom: String) =
                    if (weight.value != 0.0) {
                        weight.value?.div(Constants.CONVERT_TO_KG) to Uom.KG.name
                    } else {
                        when (goodParams.value?.uom?.toUom()) {
                            Uom.ST -> {
                                Constants.QUANTITY_DEFAULT_VALUE_1 to Uom.ST.name
                            }
                            Uom.KAR -> {
                                val uomInfo = goodParams.value
                                uomInfo?.umrez?.toInt()?.div(uomInfo.umren.toDouble()) to Uom.KAR.name
                            }
                            else -> {
                                Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.DEFAULT.name
                            }
                        }
                    }
            quantityField.value = quantity.toString()
            suffix.value = uom
        }
    }

    private fun setDateInfo() {
        launchUITryCatch {
            if (selectedDate.value == 0) producerDate = dateInfoField.value.orEmpty() else selfLifeDate = dateInfoField.value.orEmpty()
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
            ).either(::handleFailure, warehouseResult::setValue)
            warehouseResult.value?.let { warehouseResult ->
                warehouseSender.value = warehouseResult.warehouseList?.map { it.warehouseName }
                warehouseReceiver.value = warehouseResult.warehouseList?.map { it.warehouseName }
            }
        }
    }

    private fun setContainerInfo() {
        launchUITryCatch {

        }
    }

    fun onClickComplete() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val prodCodeSelectedProducer = goodParams.value?.producers?.producerCode?.getOrNull(selectedProducer.value
                    ?: 0).orEmpty()
            val warehouseSenderSelected = warehouseSender.value?.getOrNull(selectedWarehouseSender.value
                    ?: 0).orEmpty()
            val warehouseReceiverSelected = warehouseReceiver.value?.getOrNull(selectedWarehouseReceiver.value
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
                            codeCont = "codeCont",
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
        const val CONST_VALUE_23 = "23"
        const val CONST_VALUE_24 = "24"
        const val CONST_VALUE_27 = "27"
        const val CONST_VALUE_28 = "28"

        const val PROD_DATE = "Дата производства"
        const val SELF_LIFE = "Срок годности"
    }
}