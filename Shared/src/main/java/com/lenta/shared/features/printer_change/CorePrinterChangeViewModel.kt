package com.lenta.shared.features.printer_change

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener

abstract class CorePrinterChangeViewModel : CoreViewModel(), OnPositionClickListener {
    abstract fun onClickBack()
    abstract fun onClickApp()
    abstract fun onBackPressed()

    val printers: MutableLiveData<List<PrinterUi>> = MutableLiveData()
    val printersNames: MutableLiveData<List<String>> = printers.map { printers ->
        printers?.map { it.name }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedDescription: MutableLiveData<String> = selectedPosition.map {
        it?.let { position ->
            printers.value?.getOrNull(position)?.description
        }
    }

    init {
        //todo это для тестового отображения принтеров, после реализции restа удалить эти строки
        val p1 = PrinterUi("1-GRP601", "ТК-601 Списание")
        val p2 = PrinterUi("2-GRP602", "ТК-602 Списание")
        val p3 = PrinterUi("3-GRP603", "ТК-603 Списание")
        printers.value = listOf(p1, p2, p3)
        selectedPosition.value = 0
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}

data class PrinterUi(
        val name: String,
        val description: String

)
