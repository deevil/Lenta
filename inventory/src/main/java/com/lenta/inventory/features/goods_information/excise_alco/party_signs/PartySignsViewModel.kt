package com.lenta.inventory.features.goods_information.excise_alco.party_signs

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PartySignsViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: ICoreNavigator

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val manufacturersName: MutableLiveData<List<String>> = MutableLiveData()

    val stampLength: MutableLiveData<Int> = MutableLiveData()

    val bottlingDate: MutableLiveData<String> = MutableLiveData("")

    val enabledNextBtn: MutableLiveData<Boolean> = bottlingDate.map {
        isCorrectDate(it)
    }

    fun onClickNext() {
        screenNavigator.goBackWithArgs(Bundle().apply {
            putInt("stampLength", stampLength.value!!)
            putString("manufacturerCode", selectedPosition.value.toString())
            putString("bottlingDate", bottlingDate.value)
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun isCorrectDate(checkDate: String?): Boolean {
        return try {
            val formatter = SimpleDateFormat("dd.MM.yyyy")
            val date = formatter.parse(checkDate)
            !(checkDate != formatter.format(date) || date!! > timeMonitor.getServerDate())
        } catch (e: Exception) {
            false
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}
