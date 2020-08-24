package com.lenta.bp16.features.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.movement.params.ProductInfoParams
import com.lenta.bp16.model.movement.result.ProductInfoResult
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.bp16.repository.IRepoInMemoryHolder
import com.lenta.bp16.request.MovementNetRequest
import com.lenta.bp16.request.MovementParams
import com.lenta.bp16.request.ProductInfoNetRequest
import com.lenta.bp16.request.StockLockRequestResult
import com.lenta.bp16.request.pojo.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var productInfoNetRequest: ProductInfoNetRequest

    @Inject
    lateinit var movementNewRequest: MovementNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var database: DatabaseRepository



    val deviceIp = MutableLiveData("")

    val weightBarcode = listOf(CONST_VALUE_23, CONST_VALUE_24, CONST_VALUE_27, CONST_VALUE_28)

    val selectedEan = MutableLiveData("")

    /**Количество*/
    val quantityField = MutableLiveData("")
    val requestFocusQuantityField = MutableLiveData(true)

    val producerList: MutableList<ProducerInfo> = mutableListOf()
    val productList: MutableList<ProductInfo> = mutableListOf()
    val setList: MutableList<SetInfo> = mutableListOf()

    /**Производитель*/
    var producerName: MutableLiveData<List<String>> = MutableLiveData()
    val selectedProducer = MutableLiveData(0);
    val producerClicked = object : OnPositionClickListener{
        override fun onClickPosition(position: Int) {
            selectedProducer.value = position
        }

    }

    /**Дата производства и срок годности*/
    val dateInfoField = MutableLiveData("")
    val dateInfo: MutableLiveData<List<String>> = MutableLiveData()
    val selectedDate = MutableLiveData(0)
    val dateClicked = object : OnPositionClickListener{
        override fun onClickPosition(position: Int) {
            selectedDate.value = position
        }

    }
    val requestFocusDateInfoField = MutableLiveData(true)

    /**Склад отправитель*/
    val warehouseSender: MutableLiveData<List<String>> = MutableLiveData()
    val selectedWarehouseSender = MutableLiveData(0)
    val warehouseSenderClicked = object : OnPositionClickListener{
        override fun onClickPosition(position: Int) {
            selectedWarehouseSender.value = position
        }

    }

    /**Склад получатель*/
    val warehouseReceiver: MutableLiveData<List<String>> = MutableLiveData()
    val selectedWarehouseReceiver = MutableLiveData(0)
    val warehouseReceiverClicked = object : OnPositionClickListener{
        override fun onClickPosition(position: Int) {
            selectedWarehouseReceiver.value = position
        }

    }

    /**Тара*/
    val containerField = MutableLiveData("")

    val enabledCompleteButton = quantityField.map { !it.isNullOrBlank() }

    var suffix: String = Uom.KG.name

    init {
        setGoodInfo()
        setProdInfo()
        setStockInfo()
        setDateInfo()
        setContainerInfo()
    }

    private fun setGoodInfo() {
        launchUITryCatch {
            val ean = selectedEan.value.toString()
            val goodInfo = database.getGoodByEan(ean)
            var weight: Int? = 0
            /**Проверка на весовой ШК*/
            if (weightBarcode.contains(ean.substring(0 until 2))) {
                weight = ean.takeLast(6).take(5).toInt()
            }
            /**Расчет количества и единиц измерения*/
            val (quantity: Int?, uom: String) =
                    if (weight != 0) {
                        weight?.div(Constants.CONVERT_TO_KG) to Uom.KG.name
                    } else {
                        when (goodInfo?.uom) {
                            Uom.ST -> {
                                Constants.QUANTITY_DEFAULT_VALUE_1 to Uom.ST.name
                            }
                            Uom.KAR -> {
                                val uomInfo = database.getEanInfoByEan(goodInfo.ean)
                                uomInfo?.umrez?.div(uomInfo.umren) to Uom.KAR.name
                            }
                            else -> {
                                Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.DEFAULT.name
                            }
                        }
                    }
            quantityField.value = quantity.toString()
            suffix = uom
        }
    }

    private fun setProdInfo() {

        /*launchUITryCatch {
            val goodInfo = database.getGoodByEan(selectedEan.value.toString())
            val sapCode = goodInfo?.matcode?.takeLast(6)
            val ean = Ean(
                    ean = selectedEan.value
            )
            val matnr = Product(
                    matnr = sapCode
            )
            productInfoNetRequest(ProductInfoParams(
                    ean = listOf(ean),
                    matnr = listOf(matnr)
            )).either(::handleFailure) { productInfoResult ->
                handleLoadProductInfoResult(productInfoResult)
            }
        }*/
    }

    private fun handleLoadProductInfoResult(result: ProductInfoResult) {
        launchUITryCatch {
            with(result) {
               // ProductInfoResult(producers = producerList, product = productList, set = setList)

            }
        }
    }

    private fun handleSuccess(stockLockRequestResult: StockLockRequestResult) {
        val stockLockRequestResult = stockLockRequestResult
        Logg.d { "stockLockRequestResult:$stockLockRequestResult" }
    }

    private fun setDateInfo() {
        launchUITryCatch {

        }
    }

    private fun setStockInfo() {
        launchUITryCatch {

        }
    }

    private fun setContainerInfo() {
        launchUITryCatch {

        }
    }

    fun onClickComplete() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val result = movementNewRequest(
                    params = MovementParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            matnr = "good.matnr",
                            prodCode = "prod.prodCode",
                            dateProd = "dateProd",
                            expirDate = "expirDate",
                            lgortExport = "lgortExport",
                            lgortImport = "lgortImport",
                            codeCont = "codeCont",
                            factQnt = quantityField.value.toString(),
                            buom = suffix,
                            deviceIP = deviceIp.toString(),
                            personnelNumber = "personnelNumber"
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
    }
}