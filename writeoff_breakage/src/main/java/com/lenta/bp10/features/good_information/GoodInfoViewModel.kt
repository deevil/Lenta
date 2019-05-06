package com.lenta.bp10.features.good_information

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener

class GoodInfoViewModel : CoreViewModel(), OnPositionClickListener {
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
        //TODO (DB) нужно удалить фейковые данные
        writeOffReasonTitles.value = listOf("Лом бой", "Поврежд.целост.ткани", "Температурный режим", "Нарушен тврн. вида")
        count.value = "1 шт"
        totalCount.value = "1 шт"

    }

    override fun onClickPosition(position: Int) {

    }


}
