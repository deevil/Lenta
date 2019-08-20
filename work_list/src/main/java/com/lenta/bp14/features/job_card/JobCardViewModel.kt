package com.lenta.bp14.features.job_card

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class JobCardViewModel : CoreViewModel() {


    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    private lateinit var taskNumber: String

    val taskTypeNames: MutableLiveData<List<String>> = MutableLiveData(listOf("Рабочий список", "Сверка цен", "Чек лист", "Невыставленный товар"))
    val selectedTaskTypePosition: MutableLiveData<Int> = MutableLiveData(0)
    val enabledChangeTaskType: MutableLiveData<Boolean> = MutableLiveData(true)
    val taskName = MutableLiveData("taskName")
    val description = MutableLiveData("Содержание описания")
    val comment = MutableLiveData("Содержание комментария")

    fun getMarket(): String {
        return sessionInfo.market!!
    }

    fun setTaskNumber(taskNumber: String) {
        this.taskNumber = taskNumber
    }

    fun onClickNext() {
        screenNavigator.openSalesOfGoodsScreen()
    }

    val onClickTaskTypes = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedTaskTypePosition.value = position
        }
    }


}
