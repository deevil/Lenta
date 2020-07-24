package com.lenta.bp7.features.other

import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.EnteredCode
import com.lenta.bp7.data.model.GoodInfo
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

abstract class AddGoodViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData


    protected fun addGoodByEan(ean: String) {
        Logg.d { "Entered EAN: $ean" }
        launchUITryCatch {
            val goodInfo = database.getGoodInfoByEan(ean)
            if (goodInfo != null) {
                checkData.addGood(goodInfo)
                openGoodInfoScreen()
            } else {
                if (checkData.getCurrentGood()?.ean == ean) {
                    checkData.addGood(GoodInfo(enteredCode = EnteredCode.EAN, ean = ean))
                    openGoodInfoScreen()
                } else {
                    // Подтверждение - Неизвестный штрихкод. Товар определить не удалось. Все равно использовать этот штрихкод? - Назад / Да
                    navigator.showUnknownGoodBarcode(barCode = ean) {
                        checkData.addGood(GoodInfo(enteredCode = EnteredCode.EAN, ean = ean))
                        openGoodInfoScreen()
                    }
                }
            }
        }
    }

    protected fun addGoodByMaterial(material: String) {
        Logg.d { "Entered MATERIAL: $material" }
        launchUITryCatch {
            val goodInfo: GoodInfo? = database.getGoodInfoByMaterial("000000000000$material")
            if (goodInfo != null) {
                checkData.addGood(goodInfo)
                openGoodInfoScreen()
            } else {
                // Сообщение - Данный товар не найден в справочнике
                navigator.showGoodNotFound()
            }
        }
    }

    protected fun addGoodByMatcode(matcode: String) {
        Logg.d { "Entered MATCODE: $matcode" }
        launchUITryCatch {
            val goodInfo: GoodInfo? = database.getGoodInfoByMatcode(matcode)
            if (goodInfo != null) {
                checkData.addGood(goodInfo)
                openGoodInfoScreen()
            } else {
                // Сообщение - Данный товар не найден в справочнике
                navigator.showGoodNotFound()
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