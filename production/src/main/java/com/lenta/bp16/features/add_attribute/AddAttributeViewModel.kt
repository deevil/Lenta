package com.lenta.bp16.features.add_attribute

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.AddAttributeInfo
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ProducerDataStatus
import com.lenta.bp16.model.ZPartDataInfo
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.ingredients_use_case.get_data.GetAddAttributeInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetProducerDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.get_data.GetZPartDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.set_data.SetAddAttributeInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.set_data.SetProducerDataInfoUseCase
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.getFormattedDate
import javax.inject.Inject

class AddAttributeViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var producerDataInfoUseCase: GetProducerDataInfoUseCase

    @Inject
    lateinit var zPartDataInfoUseCase: GetZPartDataInfoUseCase

    @Inject
    lateinit var saveProducerDataInfoUseCase: SetProducerDataInfoUseCase

    @Inject
    lateinit var setAddAttributeInfoUseCase: SetAddAttributeInfoUseCase

    private val producerDataInfo = MutableLiveData<List<ProducerDataInfo>>()
    private val zPartDataInfo = MutableLiveData<List<ZPartDataInfo>>()

    val producerNameList = producerDataInfo.switchMap {
        asyncLiveData<List<String>> {
            val producerNameList = it.map { it.prodName.orEmpty() }
            emit(producerNameList)
        }
    }

    val selectedProducerPosition = MutableLiveData(0)

    val dateInfoField = MutableLiveData<String>("")
    private var productionDate = ""

    /** Условие отображения ошибки, если лист производителей заполнен с пробелами */
    private val alertNotFoundProducerName = MutableLiveData<Boolean>()

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        val condition = checkZPartProducerVisibleCondition().first
        alertNotFoundProducerName.value = checkZPartProducerVisibleCondition().second
        condition
    }

    private fun checkZPartProducerVisibleCondition(): Pair<Boolean, Boolean> {

        val producerVisibleCondition = zPartDataInfo.switchMap {
            asyncLiveData<List<String>> {
                val zPartProducerNameList = it.map { it.prodName.orEmpty() }
                emit(zPartProducerNameList)
            }
        }

        val producersList = producerVisibleCondition.value.orEmpty()

        var fullItemCount = 0
        for (zPartName in producersList) {
            if (zPartName.isNotEmpty()) {
                fullItemCount++ //Считаем количество не пустых полей в списке
            }
        }

        val visibleStatus = when {
            (fullItemCount == 0) -> ProducerDataStatus.GONE
            (fullItemCount == producersList.size) -> ProducerDataStatus.VISIBLE
            else -> ProducerDataStatus.ALERT
        }

        return when(visibleStatus){
            ProducerDataStatus.GONE -> false to false
            ProducerDataStatus.VISIBLE -> true to false
            ProducerDataStatus.ALERT -> true to true
        }
    }

    fun updateData(){
        launchUITryCatch {
            producerDataInfo.value = producerDataInfoUseCase.invoke()
            zPartDataInfo.value = zPartDataInfoUseCase.invoke()
            if(alertNotFoundProducerName.value == true){
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

            val result = AddAttributeInfo(
                    prodName = producerSelected,
                    prodDate = productionDate
            )
            setAddAttributeInfoUseCase(listOf(result))
            navigator.goBack()
        }
    }

    companion object {
        const val DATE_LENGTH = 10
    }
}