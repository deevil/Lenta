package com.lenta.bp16.features.add_attribute

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.BatchNewDataInfo
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.ingredients_use_case.get_data.GetProducerDataInfoUseCase
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
    lateinit var saveProducerDataInfoUseCase: SetProducerDataInfoUseCase

    private val producerDataInfo by unsafeLazy {
        MutableLiveData<List<ProducerDataInfo>>()
    }

    val producerNameList by unsafeLazy {
        producerDataInfo.switchMap {
            asyncLiveData<List<String>> {
                val producerNameList = it.map { it.prodName.orEmpty() }
                emit(producerNameList)
            }
        }
    }

    val selectedProducerPosition = MutableLiveData(0)

    val dateInfoField = MutableLiveData<String>("")
    private var productionDate = ""

    /** Условие отображения производителя */
    val producerVisibleCondition by unsafeLazy {
        producerNameList.mapSkipNulls {
            it.isNotEmpty()
        }
    }

    init {
        launchUITryCatch {
            producerDataInfo.value = producerDataInfoUseCase.invoke()
        }
    }

    private fun setDateInfo(){
        var date = dateInfoField.value.orEmpty()
        if(date.isNotEmpty() && date.length == DATE_LENGTH){
            date = getFormattedDate(date, Constants.DATE_FORMAT_dd_mm_yyyy, Constants.DATE_FORMAT_yyyyMMdd )
        }
        productionDate = date
    }

    /**Проверка даты на корректность*/
    private fun checkDate(): Boolean {
        val checkDate = dateInfoField.value.orEmpty()
        val splitCheckDate = checkDate.split(".")
        val day = splitCheckDate[0].toInt()
        val month = splitCheckDate[1].toInt()
        val year = splitCheckDate[2].toInt()
        val monthWith31Days = listOf(1, 3, 5, 7, 8, 10, 12)
        val monthWith30Days = listOf(4, 6, 9, 11)
        return if (year in 2000..2100 && month in 1..12 && day in 1..31) {
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
    }

    fun onClickComplete() {
        navigator.showProgressLoadingData()
        setDateInfo()
        val dateIsCorrect = checkDate()
        if (!dateIsCorrect) {
            navigator.hideProgress()
            navigator.showAlertWrongDate()
        } else {
            //val selectedProducerIndex = selectedProducerPosition.getOr
            val result = BatchNewDataInfo(
                    prodCode = producerNameList.value.getOrNull(selectedProducerPosition.value)
            )
        }
    }

    companion object{
        const val DATE_LENGTH = 10
    }

}