package com.lenta.bp16.features.add_attribute

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.AddAttributeInfo
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ProducerDataStatus
import com.lenta.bp16.model.ZPartDataInfo
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.ingredients_use_case.get_data.GetProducerDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetZPartDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.set_data.SetAddAttributeInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.set_data.SetProducerDataInfoUseCase
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.getFormattedDate
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddAttributeViewModel : CoreViewModel(), IZpartVisibleConditions {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest

    @Inject
    lateinit var producerDataInfoUseCase: GetProducerDataInfoUseCase

    @Inject
    lateinit var zPartDataInfoUseCase: GetZPartDataInfoUseCase

    @Inject
    lateinit var saveProducerDataInfoUseCase: SetProducerDataInfoUseCase

    @Inject
    lateinit var setAddAttributeInfoUseCase: SetAddAttributeInfoUseCase

    private val producerDataInfo = MutableLiveData<List<ProducerDataInfo>>()
    override val zPartDataInfo = MutableLiveData<List<ZPartDataInfo>>()
    val shelfLife = MutableLiveData<String>() //Срок годности

    val producerNameList = producerDataInfo.switchMap {
        asyncLiveData<List<String>> {
            val producerNameList = it.map { it.prodName.orEmpty() }
            emit(producerNameList)
        }
    }

    val producerCodeList = producerDataInfo.switchMap {
        asyncLiveData<List<String>> {
            val producerCodeList = it.map { it.prodCode.orEmpty() }
            emit(producerCodeList)
        }
    }

    val selectedProducerPosition = MutableLiveData(0)

    private var productionDate = ""
    private var productionTime = ""
    val dateInfoField = MutableLiveData<String>("")
    val timeField = MutableLiveData<String>("")

    /** Условие отображения ошибки, если лист производителей заполнен с пробелами */
    private val alertNotFoundProducerName = MutableLiveData<Boolean>()

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        asyncLiveData<Boolean> {
            val cond = producerConditions
            val condition = cond.first
            alertNotFoundProducerName.value = cond.second
            emit(condition)
        }
    }

    val timeFieldVisibleCondition by unsafeLazy {
        false
        //TODO: Реализовать логику отображения поля времени
       /* var convertedTime: Int
        shelfLife.value?.let {
            val time = shelfLife.value?.toInt()
            convertedTime = time?.div(HOURS_IN_DAY) ?: 0
        }
        val timeVisibleCondition = if( convertedTime )*/
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

    fun updateData(){
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
        var date = dateInfoField.value.orEmpty()
        if (date.isNotEmpty() && date.length == DATE_LENGTH) {
            date = getFormattedDate(date, Constants.DATE_FORMAT_dd_mm_yyyy, Constants.DATE_FORMAT_yyyyMMdd)
        }
        productionDate = date
    }

    /**Проверка даты на корректность*/
    private fun checkDate(): Boolean {
        val checkDate = dateInfoField.value.orEmpty()
        val correctDate = if (checkDate.isNotEmpty() && checkDate.length == DATE_LENGTH) {
            val splitCheckDate = checkDate.split(".")
            val day = splitCheckDate[0].toInt()
            val month = splitCheckDate[1].toInt()
            val year = splitCheckDate[2].toInt()
            val monthWith31Days = listOf(1, 3, 5, 7, 8, 10, 12)
            val monthWith30Days = listOf(4, 6, 9, 11)
            if (year in 2000..2100 && month in 1..12 && day in 1..31) {
                if (monthWith31Days.contains(month)) {
                    day <= 31
                } else if (monthWith30Days.contains(month) && month != 2) {
                    day <= 30
                } else if (year % 4 == 0) {
                    day <= 29
                } else {
                    day <= 28
                }
            } else {
                false
            }
        } else {
            false
        }
        return correctDate
    }

    fun onClickComplete() = launchUITryCatch {
        setDateInfo()
        val dateIsCorrect = checkDate()
        if (!dateIsCorrect) {
            navigator.showAlertWrongDate()
        } else {
            val producerIndex = selectedProducerPosition.getOrDefaultWithNull()
            val producerSelected = producerNameList.getOrEmpty(producerIndex)
            val prodCode = producerCodeList.getOrEmpty(producerIndex)

            val result = AddAttributeInfo(
                    prodName = producerSelected,
                    prodTime = "",
                    prodCode = prodCode,
                    prodDate = productionDate
            )
            setAddAttributeInfoUseCase(listOf(result))
            navigator.goBack()
        }
    }

    companion object {
        const val DATE_LENGTH = 10
        const val HOURS_IN_DAY = 24
    }
}