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
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectGoodViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var failureInterpreter: IFailureInterpreter

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var database: IDatabaseRepo

    val good: MutableLiveData<Good> = MutableLiveData()

    val selectBarcodeField: MutableLiveData<String> = MutableLiveData()

    val barcodeScan = onClickBarcodeScanner()

    private val replaceValue = listOf("23", "24", "27", "28")

    val title: MutableLiveData<String> = MutableLiveData()

    fun onClickNext() {
        viewModelScope.launch {
            var barcode = selectBarcodeField.toString()
            if (replaceValue.contains(barcode.substring(0 until 2))) {
                barcode = barcode.replace(barcode.takeLast(6), "000000")
            }
            searchEan(barcode)
            navigator.openGoodsInfoScreen()
        }
    }

    fun onClickExit() {
        viewModelScope.launch {
            /*Выход из приложения*/
        }
    }

    private fun onClickBarcodeScanner() {
        /*получаем штрихкод с помощью сканирования*/
        /*Смотреть onScanResult */
    }

    suspend fun searchEan(ean: String) {
        when (database.getGoodInfoByEan(ean)) {
            null -> showError()
            else -> onClickNext()
        }
    }

    private fun showError() {
        viewModelScope.launch {
            navigator.showAlertGoodsNotFound()
        }
    }
}