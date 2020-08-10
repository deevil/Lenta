package com.lenta.bp16.features.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

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

    /**Производитель*/
    val manufactureName: MutableLiveData<List<String>> = MutableLiveData()

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
    }

    private fun setGoodInfo() {
        launchUITryCatch {
            val goodInfo = database.getGoodByEan(selectedEan.value.toString())
            var weight: Int? = 0
            if(weightBarcode.contains(selectedEan.value.toString().substring(0 until 2))){
                weight = selectedEan.value?.takeLast(6)?.take(5)?.toInt()
            }
            val uom: String
            var quantity: Int? = Constants.QUANTITY_DEFAULT_VALUE_0

            if (weight != 0){
                quantity = weight?.div(Constants.CONVERT_TO_KG)
                uom = Uom.KG.name
            }else{
                when(goodInfo?.uom){
                    Uom.ST -> {
                        quantity = Constants.QUANTITY_DEFAULT_VALUE_1
                        uom = Uom.ST.name
                    }
                    Uom.KAR ->{
                        val uomInfo = database.getEanInfoByEan(selectedEan.value.toString())
                        quantity = uomInfo?.umrez?.div(uomInfo.umren)
                        uom = Uom.KAR.name
                    }
                    Uom.G -> {
                        uom = Uom.G.name
                    }
                    else -> {
                        uom = Uom.DEFAULT.name
                    }
                }
            }

            quantityField.value = "$quantity $uom"
        }
    }

    fun onClickComplete() {
        launchUITryCatch {
            //TODO показать сообщение
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