package com.lenta.bp9.features.goods_information.excise_alco_pge.excise_alco_stamp_acc_pge.batch_signs

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import java.text.SimpleDateFormat
import javax.inject.Inject

class ExciseAlcoStampPGEBatchSignsViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val manufacturersName: MutableLiveData<List<String>> = MutableLiveData()

    val bottlingDate: MutableLiveData<String> = MutableLiveData("")

    val enabledNextBtn: MutableLiveData<Boolean> = bottlingDate.map {
        isCorrectDate(it)
    }

    init {
        launchUITryCatch {
            manufacturersName.value = repoInMemoryHolder.manufacturers.value?.map {
                it.name
            }
        }
    }

    fun onClickNext() {
        screenNavigator.goBackWithArgs(Bundle().apply {
            putInt("manufacturerSelectedPosition", selectedPosition.value  ?: 0)
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
