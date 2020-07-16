package com.lenta.movement.features.task.eo.eo_insides

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class TaskEOMergeEOInsidesViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val selectionsHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val eo by unsafeLazy { MutableLiveData<ProcessingUnit>() }

    val goodsItemList by unsafeLazy {
        eo.map { eo ->
            eo?.let { eoValue ->
                eoValue.goods?.mapIndexed { index, good ->
                    SimpleListItem(
                            number = index + 1,
                            title = good.materialNumber.orEmpty(),
                            subtitle = "",
                            countWithUom = "${good.quantity} ${good.orderUnits}",
                            isClickable = false
                    )
                }
            }
        }
    }


    val isExcludeBtnEnabled by unsafeLazy {
        selectionsHelper.selectedPositions.map { setOfSelectedItems ->
            setOfSelectedItems?.size?.let {
                it > 0
            }
        }
    }

    fun getTitle(): String {
        return eo.value?.let {
            "$EO-${it.processingUnitNumber}"
        }.orIfNull {
            Logg.e {
                "eoList is null"
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