package com.lenta.bp18.features.select_good

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.models.core.BarcodeInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectGoodViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var database: IDatabaseRepo

    val good: MutableLiveData<Good> = MutableLiveData()

    val barcodeField = MutableLiveData("")
    val editTextFocus = MutableLiveData<Boolean>()

    //val barcodeField ="2499999640787"

    val barcodeScan = onClickBarcodeScanner()

    private val replaceValue = listOf("23", "24", "27", "28")

    init {

    }


    fun onClickNext() {
        viewModelScope.launch {
            //TODO Упадет с ошибкой, если не ввести значение
            var barcode = barcodeField.value ?: ""
            Logg.d { "Введенный ШК: $barcode" }
            if (replaceValue.contains(barcode.substring(0 until 2))) {
                barcode = barcode.replace(barcode.takeLast(6), "000000")
                Logg.d { "ШК после изменения: $barcode" }
            }
            searchEan(barcode)
        }
    }

    fun onClickExit() {
        /*Выход из приложения*/
    }

    private fun onClickBarcodeScanner() {
        /*получаем штрихкод с помощью сканирования*/
        /*Смотреть onScanResult */
    }

    private fun searchEan(ean: String) {
        Logg.d { "Entered EAN: $ean" }
        viewModelScope.launch {
            when (ean/*database.getGoodInfoByEan(ean)*/) {
                null -> showError()
                else -> openGoodInfoScreen()
            }
        }
    }

    private fun openGoodInfoScreen() {
        navigator.openGoodsInfoScreen()
    }

    private fun showError() {
        navigator.showAlertGoodsNotFound()
    }
}