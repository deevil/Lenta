package com.lenta.movement.features.task.eo.eo_insides

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class TaskEOMergeEOInsidesViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var formatter: IFormatter

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val eo by unsafeLazy { MutableLiveData<ProcessingUnit>() }

    val goodsItemList by unsafeLazy {
        eo.map { eo ->
            eo?.let { eoValue ->
                eoValue.goods?.mapIndexed { index, good ->
                    val goodNumber = good.materialNumber
                    val goodTitle = getGoodTitle(goodNumber)
                    val orderUnits = getOrderUnits(good.orderUnits)
                    SimpleListItem(
                            number = index + 1,
                            title = goodTitle,
                            subtitle = "",
                            countWithUom = "${good.quantity?.toDouble()?.toInt()} $orderUnits",
                            isClickable = false
                    )
                }
            }
        }
    }

    private fun getGoodTitle(goodNumber: String?)
            = "${getMaterialLastSix(goodNumber)} ${taskManager.getGoodName(goodNumber)}"

    private fun getMaterialLastSix(materialNumber: String?): String {
        return materialNumber?.let {
            if (materialNumber.length > 6)
                materialNumber.substring(materialNumber.length - 6)
            else materialNumber
        }.orIfNull {
            Logg.e { "materialNumber Null" }
            ERROR
        }
    }

    private fun getOrderUnits(orderUnits : String?) : String {
        return orderUnits?.let { units ->
            formatter.getOrderUnitsNameByCode(units)
        }.orIfNull {
            Logg.e { "UOM null" }
            ERROR
        }
    }

    fun getTitle(): String {
        return eo.value?.let {
            "$EO-${it.processingUnitNumber}"
        }.orIfNull {
            Logg.e {
                "eo is null"
            }
            ERROR
        }
    }

    fun onBackPressed() {
        screenNavigator.goBack()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

    fun onDigitPressed(digit: Int) = Unit


    companion object {
        private const val EO = "ЕО"
        private const val ERROR = "Ошибка"
    }
}