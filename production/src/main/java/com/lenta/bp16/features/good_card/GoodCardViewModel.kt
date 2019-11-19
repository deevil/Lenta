package com.lenta.bp16.features.good_card

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.pojo.Good
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import javax.inject.Inject

class GoodCardViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager


    val title by lazy {
        "000021 - Форель заморож."
    }

    val good: MutableLiveData<Good> by lazy { taskManager.getCurrentGood() }

    val weight = MutableLiveData("")

    val totalWeight = good.combineLatest(weight).map {
        val enteredWeight = it?.second?.toDoubleOrNull() ?: 0.0
        val totalWeight = it?.first?.total

        "${totalWeight.sumWith(enteredWeight).dropZeros()} ${it?.first?.units?.name}"
    }

    val planned by lazy {
        good.map { good ->
            "${good?.planned} ${good?.units?.name}"
        }
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