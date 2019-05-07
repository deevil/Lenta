package com.lenta.bp10.features.good_information

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    private lateinit var goodCode: String

    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData()

    val totalCount: MutableLiveData<String> = MutableLiveData()

    fun setGoodCode(goodCode: String) {
        this.goodCode = goodCode
    }

    val goodTitle = MutableLiveData("")

    init {
        viewModelScope.launch {
            writeOffReasonTitles.value = processServiceManager.getWriteOffTask()?.taskDescription?.moveTypes

            //TODO remove fake data
            count.value = "1 шт"
            totalCount.value = "1 шт"
        }


    }

    override fun onClickPosition(position: Int) {

    }


}
