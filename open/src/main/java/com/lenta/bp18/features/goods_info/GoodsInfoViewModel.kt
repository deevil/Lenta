package com.lenta.bp18.features.goods_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodsInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

/*    @Inject
    lateinit var manager: ITaskManager

    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }*/

    val deviceIp = MutableLiveData("")

    val weightField = MutableLiveData("0")

    private val entered = weightField.map{
        it?.toDoubleOrNull() ?: 0.0
    }

    val completeEnabled = entered.map{
        val enabledValue = it ?: 0.0
    }

    fun onClickComplete(){
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            //TODO Обработать клик
        }
    }
}