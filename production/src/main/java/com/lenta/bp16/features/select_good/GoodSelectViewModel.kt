package com.lenta.bp16.features.select_good

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.GoodParams
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodSelectViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var database: DatabaseRepository

    val deviceIp = MutableLiveData("")

    val eanValue = MutableLiveData("")

    val enteredEanField = MutableLiveData("")
    val requestFocusEnteredEanField = MutableLiveData(true)

    val enabledNextButton = enteredEanField.map { !it.isNullOrBlank() }

    private fun searchGood() {
        launchUITryCatch {
            val goodEan = database.getGoodByEan(eanValue.value.toString())
            goodEan?.let {
                val ean = it.ean
                val material = it.getFormattedMaterial()
                val name = it.name
                val goodParams = GoodParams(ean = ean, material = material, name = name)
                navigator.openGoodInfoScreen(goodParams)
            } ?: navigator.showAlertGoodNotFound()
        }
    }

    fun onClickNext() {
        eanValue.value = enteredEanField.value
        searchGood()
    }

    fun onScanResult(data: String) {
        eanValue.value = data
        searchGood()
    }

    fun onClickMenu() {
        navigator.openMainMenuScreen()
    }

}