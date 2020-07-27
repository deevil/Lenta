package com.lenta.bp16.features.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    val deviceIp = MutableLiveData("")

    val weightField = MutableLiveData("0")

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


    private val entered = weightField.map{
        it?.toDoubleOrNull() ?: 0.0
    }

    val completeEnabled = entered.map{
        it ?: 0.0 != 0.0
    }

    fun onClickComplete(){
        launchUITryCatch {
            //TODO показать сообщение
            navigator.goBack()
        }
    }
}