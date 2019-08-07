package com.lenta.bp7.features.other

import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.SapCodeType
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.GoodInfo
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.constants.Constants.COMMON_SAP_LENGTH
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.launch
import javax.inject.Inject


abstract class AddGoodViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData


    protected fun addGoodBySapCode(sapCode: String) {
        Logg.d { "Entered SAP-code: $sapCode" }
        viewModelScope.launch {
            val goodInfo: GoodInfo? = when (sapCode.length) {
                COMMON_SAP_LENGTH -> database.getGoodInfoBySapCode("000000000000$sapCode", SapCodeType.MATERIAL)
                else -> database.getGoodInfoBySapCode(sapCode, SapCodeType.MATCODE)
            }
            if (goodInfo != null) {
                checkData.addGood(goodInfo)
                openGoodInfoScreen()
            } else {
                // Сообщение - Данный товар не найден в справочнике
                navigator.showGoodNotFound()
            }
        }
    }

    protected fun addGoodByBarCode(barCode: String) {
        Logg.d { "Entered BAR-code: $barCode" }
        viewModelScope.launch {
            val goodInfo = database.getGoodInfoByBarCode(barCode)
            if (goodInfo != null) {
                checkData.addGood(goodInfo)
                openGoodInfoScreen()
            } else {
                if (checkData.getCurrentGood()?.barCode == barCode) {
                    checkData.addGood(GoodInfo(barCode = barCode))
                    openGoodInfoScreen()
                } else {
                    // Подтверждение - Неизвестный штрихкод. Товар определить не удалось. Все равно использовать этот штрихкод? - Назад / Да
                    navigator.showUnknownGoodBarcode(
                            barCode = barCode) {
                        checkData.addGood(GoodInfo(barCode = barCode))
                        openGoodInfoScreen()
                    }
                }
            }
        }
    }

    protected fun openGoodInfoScreen() {
        // TODO Раскомментировать для тестирования разных сценариев проверки
        /*checkData.countFacings = true
        checkData.checkEmptyPlaces = true*/

        if (checkData.countFacings) {
            navigator.openGoodInfoFacingScreen()
        } else {
            navigator.openGoodInfoScreen()
        }
    }
}