package com.lenta.bp16.features.material_remake_details.add_attribute

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.AddAttributeProdInfo
import com.lenta.bp16.model.IAttributeManager
import com.lenta.bp16.model.ingredients.ui.MaterialIngredientDataInfoUI
import com.lenta.bp16.model.ingredients.ui.ProducerDataInfoUI
import com.lenta.bp16.model.ingredients.ui.ZPartDataInfoUI
import com.lenta.bp16.platform.base.IZpartVisibleConditions
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.IDatabaseRepository
import com.lenta.bp16.request.ingredients_use_case.get_data.GetProducerDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetZPartDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.set_data.SetProducerDataInfoUseCase
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject
import kotlin.properties.Delegates

class MaterialAttributeViewModel : CoreViewModel(), IZpartVisibleConditions {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var database: IDatabaseRepository

    @Inject
    lateinit var attributeManager: IAttributeManager

    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest

    @Inject
    lateinit var producerDataInfoUseCase: GetProducerDataInfoUseCase

    @Inject
    lateinit var zPartDataInfoUseCase: GetZPartDataInfoUseCase

    @Inject
    lateinit var saveProducerDataInfoUseCase: SetProducerDataInfoUseCase

    private val producerDataInfo = MutableLiveData<List<ProducerDataInfoUI>>()
    override val zPartDataInfo = MutableLiveData<List<ZPartDataInfoUI>>()
    val materialIngredient = MutableLiveData<MaterialIngredientDataInfoUI>()

    // значение параметра OBJ_CODE из родительского компонента заказа
    var parentCode: String by Delegates.notNull()
    val producerNameList = producerDataInfo.switchMap {
        asyncLiveData<List<String>> {
            val producerNameList = it.map { it.prodName }
            emit(producerNameList)
        }
    }

    val producerCodeList = producerDataInfo.switchMap {
        asyncLiveData<List<String>> {
            val producerCodeList = it.map { it.prodCode }
            emit(producerCodeList)
        }
    }

    val selectedProducerPosition = MutableLiveData(0)

    private var productionDate = ""
    private var productionTime = ""
    val dateInfoField = MutableLiveData<String>()
    val timeField = MutableLiveData<String>()

    /** Условие отображения ошибки, если лист производителей заполнен с пробелами */
    private val alertNotFoundProducerName = MutableLiveData<Boolean>()

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        producerConditions.switchMap { cond ->
            asyncLiveData<Boolean> {
                val condition = cond.first
                alertNotFoundProducerName.postValue(cond.second)
                emit(condition)
            }
        }
    }

    val timeFieldVisibleCondition by unsafeLazy {
        asyncLiveData<Boolean> {
            val condition = checkTimeFieldVisibleCondition()
            emit(condition)
        }
    }

    private suspend fun checkTimeFieldVisibleCondition(): Boolean {
        var visibleCondition = true
        val timeParams = database.getPerishable()?.div(DIVIDER) ?: 0
        val shelfLife = materialIngredient.value?.shelfLife?.toInt() ?: 0
        visibleCondition = shelfLife < timeParams
        return visibleCondition
    }

    fun getServerTime() {
        launchUITryCatch {
            serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                    .orEmpty())).either(::handleFailure, ::handleSuccessServerTime)
        }
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
    }

    fun updateData() {
        launchUITryCatch {
            producerDataInfo.value = producerDataInfoUseCase()
            zPartDataInfo.value = zPartDataInfoUseCase()
            if (alertNotFoundProducerName.value == true) {
                navigator.goBack()
                navigator.showAlertProducerCodeNotFound()
            }
        }
    }

    private fun setDateInfo() {
        val date = dateInfoField.value.orEmpty()
        productionDate = date
    }

    private fun setTimeInfo() {
        var time = timeField.value.orEmpty()
        if (time.isNotEmpty() && time.length == TIME_LENGTH) {
            time += ":${timeMonitor.getServerDate().seconds}" //Добавление секунд
        }
        productionTime = time
    }

    /**Проверка даты на корректность*/
    private fun checkDate(): Boolean {
        val checkDate = dateInfoField.value.orEmpty()
        return if (checkDate.isNotEmpty() && checkDate.length == DATE_LENGTH) {
            val splitCheckDate = checkDate.split(".")
            val day = splitCheckDate[0].toInt()
            val month = splitCheckDate[1].toInt()
            val year = splitCheckDate[2].toInt()
            val monthWith31Days = listOf(1, 3, 5, 7, 8, 10, 12)
            val monthWith30Days = listOf(4, 6, 9, 11)
                when{
                    monthWith31Days.contains(month) -> day <= 31
                    monthWith30Days.contains(month) && month != 2 -> day <= 30
                    year % 4 == 0 -> day <= 29
                    month == 2 -> day <= 28
                    else -> false
                }
        } else {
            false
        }
    }

    /**Проверка времени на корректность*/
    private fun checkTime(): Boolean {
        /**Выполнять проверку времени только при отображении таймера*/
        return if (timeFieldVisibleCondition.value == true) {
            val checkTime = timeField.value.orEmpty()
            if (checkTime.isNotEmpty() && checkTime.length == TIME_LENGTH) {
                val splitCheckTime = checkTime.split(":")
                val hours = splitCheckTime[0].toInt()
                val minutes = splitCheckTime[1].toInt()
                hours in 0..23 && minutes in 0..59
            } else {
                false
            }
        } else {
            true
        }
    }

/*    */
    /**Проверка на истечение срока годности*//*
    private fun checkShelfLife(): Boolean {
        val date = dateInfoField.value.orEmpty()
        val time = timeField.value.orEmpty()

    }*/

    fun onClickComplete() = launchUITryCatch {
        setDateInfo()
        setTimeInfo()
        val dateIsCorrect = checkDate()
        val timeIsCorrect = checkTime()
        //val shelfLifeCorrect = checkShelfLife()
        when {
            !dateIsCorrect -> navigator.showAlertWrongDate()
            !timeIsCorrect -> navigator.showAlertWrongTime()
            //!shelfLifeCorrect -> navigator.showAlertShelfLifeExpired()
            else -> {
                val producerIndex = selectedProducerPosition.getOrDefaultWithNull()
                val producerSelected = producerNameList.getOrEmpty(producerIndex)
                val prodCode = producerCodeList.getOrEmpty(producerIndex)

                val result = AddAttributeProdInfo(
                        name = producerSelected,
                        time = "",
                        code = prodCode,
                        date = productionDate
                )
                attributeManager.updateCurrentAttribute(result)
                navigator.goBack()
            }
        }
    }

    companion object {
        const val DATE_LENGTH = 10
        const val TIME_LENGTH = 5

        /** Значение, на которое необходимо поделить параметр GRZ_PERISHABLE_HH */
        const val DIVIDER = 24
    }
}