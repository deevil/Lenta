package com.lenta.bp16.features.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.bp16.request.ProductInfoNetRequest
import com.lenta.bp16.request.ProductInfoParams
import com.lenta.bp16.request.ProductInfoResult
import com.lenta.bp16.request.StockNetRequest
import com.lenta.bp16.request.pojo.Ean
import com.lenta.bp16.request.pojo.ProducerInfo
import com.lenta.bp16.request.pojo.Product
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var productInfoNetRequest: ProductInfoNetRequest

    @Inject
    lateinit var stockNetRequest: StockNetRequest

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

    val producerList by unsafeLazy {

    }

    /**Производитель*/
    var manufactureName: MutableList<ProducerInfo> = mutableListOf()

    /**Дата производства и срок годности*/
    val dateInfoField = MutableLiveData("")
    val dateInfo: MutableLiveData<List<String>> = MutableLiveData()
    val requestFocusDateInfoField = MutableLiveData(true)

    /**Склад отправитель*/
    val warehouseSender: MutableLiveData<List<String>> = MutableLiveData()

    /**Склад получатель*/
    val warehouseReceiver: MutableLiveData<List<String>> = MutableLiveData()

    /**Тара*/
    val containerField = MutableLiveData("")

    val enabledCompleteButton = quantityField.map { !it.isNullOrBlank() }

    init {
        setGoodInfo()
        setProdInfo()
    }

    private fun setGoodInfo() {
        launchUITryCatch {
            val goodInfo = database.getGoodByEan(selectedEan.value.toString())
            var weight: Int? = 0
            if (weightBarcode.contains(selectedEan.value.toString().substring(0 until 2))) {
                weight = selectedEan.value?.takeLast(6)?.take(5)?.toInt()
            }

            val (quantity: Int?, uom: String) =
                    if (weight != 0) {
                        weight?.div(Constants.CONVERT_TO_KG) to Uom.KG.name
                    } else {
                        when (goodInfo?.uom) {
                            Uom.ST -> {
                                Constants.QUANTITY_DEFAULT_VALUE_1 to Uom.ST.name
                            }
                            Uom.KAR -> {
                                val uomInfo = database.getEanInfoByEan(selectedEan.value.toString())
                                uomInfo?.umrez?.div(uomInfo.umren) to Uom.KAR.name
                            }
                            Uom.G -> {
                                Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.G.name
                            }
                            else -> {
                                Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.DEFAULT.name
                            }
                        }
                    }

            quantityField.value = "$quantity $uom"
        }
    }

    private fun setProdInfo() {
        launchUITryCatch {
            val ean = Ean(
                    ean = "2425352000000"
            )
            val matnr = Product(
                    matnr = "2425352000000"
            )
            productInfoNetRequest(ProductInfoParams(
                    ean = listOf(ean),
                    matnr = listOf(matnr)
            )).either(::handleFailure){
                productInfoResult ->
                handleLoadProductInfoResult(productInfoResult)
            }
        }
    }

    private fun handleLoadProductInfoResult(result: ProductInfoResult){
        launchUITryCatch {
            with(result){
                manufactureName = producers.toMutableList()
            }
        }
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
            navigator.goBack()
        }
    }

    companion object {
        const val CONST_VALUE_23 = "23"
        const val CONST_VALUE_24 = "24"
        const val CONST_VALUE_27 = "27"
        const val CONST_VALUE_28 = "28"
    }
}