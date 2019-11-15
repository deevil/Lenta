package com.lenta.bp16.features.good_card

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class GoodCardViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title by lazy {
        "000021 - Форель заморож."
    }

    val completeEnabled = MutableLiveData(true)

    val addEnabled = MutableLiveData(true)

    val addVisibility = MutableLiveData(true)

    val getWeightVisibility = MutableLiveData(true)

    // -----------------------------

    fun onClickGetWeight() {
        // Получение веса с подключенных весов

    }

    fun onClickAdd() {
        // Добавление дополнительной тары

    }

    fun onClickComplete() {
        // Завершить обработку текущего сырья

    }

}
