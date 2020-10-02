package com.lenta.bp16.features.material_remake_details.add_attribute

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.features.ingredient_details.add_attribute.IngredientAttributeViewModel
import com.lenta.bp16.model.AddAttributeProdInfo
import com.lenta.bp16.model.managers.IAttributeManager
import com.lenta.bp16.model.ingredients.ui.MaterialIngredientDataInfoUI
import com.lenta.bp16.model.ingredients.ui.ProducerDataInfoUI
import com.lenta.bp16.model.ingredients.ui.ZPartDataInfoUI
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.Constants.HOUR_RANGE
import com.lenta.bp16.platform.Constants.MINUTES_RANGE
import com.lenta.bp16.platform.Constants.MONTH_WITH_28_DAY
import com.lenta.bp16.platform.Constants.MONTH_WITH_29_DAY
import com.lenta.bp16.platform.Constants.MONTH_WITH_30_DAY
import com.lenta.bp16.platform.Constants.MONTH_WITH_31_DAY
import com.lenta.bp16.platform.Constants.YEAR_RANGE_2000_TO_2100
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
import java.text.SimpleDateFormat
import java.util.*
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
    override val zPartDataInfos = MutableLiveData<List<ZPartDataInfoUI>>()
    val materialIngredient = MutableLiveData<MaterialIngredientDataInfoUI>()

    // значение параметра OBJ_CODE из родительского компонента заказа
    var parentCode: String by Delegates.notNull()
    val producerNameList = producerDataInfo.switchMap { producerDataInfoValue ->
        liveData {
            val producerNameList = producerDataInfoValue.map { it.prodName  }
            val producerCodes = producerDataInfoValue.map { it.prodCode  }
            producerCodeList.value = producerCodes
            emit(producerNameList)
        }
    }

    private val producerCodeList = MutableLiveData<List<String>>()

    val selectedProducerPosition = MutableLiveData(0)

    private var productionDate = ""
    private var productionTime = ""
    val dateInfoField = MutableLiveData<String>()
    val timeField = MutableLiveData<String>()

    /** Условие отображения ошибки, если лист производителей заполнен с пробелами */
    private val alertNotFoundProducerName = MutableLiveData<Boolean>()

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        producerConditions.mapSkipNulls { cond ->
            val condition = cond.first
            alertNotFoundProducerName.value = cond.second
            condition
        }
    }

    val timeFieldVisibleCondition by unsafeLazy {
        asyncLiveData<Boolean> {
            val condition = checkTimeFieldVisibleCondition()
            emit(condition)
        }
    }

    private suspend fun checkTimeFieldVisibleCondition(): Boolean {
        val visibleCondition: Boolean
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
            zPartDataInfos.value = zPartDataInfoUseCase()
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
            val leapYear = (year % 4 == 0) //Условие високосного года
            when {
                year !in YEAR_RANGE_2000_TO_2100 -> false
                monthWith31Days.contains(month) -> day <= MONTH_WITH_31_DAY
                monthWith30Days.contains(month) && month != 2 -> day <= MONTH_WITH_30_DAY
                leapYear  -> day <= MONTH_WITH_29_DAY
                month == 2 -> day <= MONTH_WITH_28_DAY
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
                hours in HOUR_RANGE && minutes in MINUTES_RANGE
            } else {
                false
            }
        } else {
            true
        }
    }

    /**Проверка на истечение срока годности*/
    private fun checkShelfLife(): Boolean {
        val date = dateInfoField.value.orEmpty()
        return if (date.isNotEmpty() && date.length == IngredientAttributeViewModel.DATE_LENGTH) {
            val shelfLife = materialIngredient.value?.shelfLife?.toLong() ?: 0
            val currentDate = timeMonitor.getServerDate().getFormattedDateLongYear()
            val sdf = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy, Locale.US)
            val shelfLifeToMillSec = shelfLife * Constants.CONVERT_TO_MILLISECOND_VALUE
            val prodDate = sdf.parse(date).time //Дата производства в миллисекундах
            val checkCurrentDate = sdf.parse(currentDate) //Дата проверки
            val expiredDateInString = sdf.format(prodDate + shelfLifeToMillSec) //Дата истечения срока годности в миллисекундах
            val expiredDate = sdf.parse(expiredDateInString)
            checkCurrentDate.before(expiredDate)
        } else {
            true
        }
    }

    fun onClickComplete() = launchUITryCatch {
        setDateInfo()
        setTimeInfo()
        val dateIsCorrect = checkDate()
        val timeIsCorrect = checkTime()
        val shelfLifeCorrect = checkShelfLife()
        when {
            !dateIsCorrect -> navigator.showAlertWrongDate()
            !timeIsCorrect -> navigator.showAlertWrongTime()
            !shelfLifeCorrect -> navigator.showAlertShelfLifeExpired()
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