package com.lenta.bp16.features.select_good

import androidx.lifecycle.MutableLiveData
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

    val enteredEanField = MutableLiveData("")
    val requestFocusEnteredEanField = MutableLiveData(true)

    val enabledNextButton = enteredEanField.map { !it.isNullOrBlank() }

    fun onClickNext(){
        launchUITryCatch {
            val goodEan = database.getGoodByEan(enteredEanField.value.toString())
            goodEan?.let {
                val ean = it.ean
                val material = it.material
                navigator.openGoodInfoScreen(ean, material)
            } ?: navigator.showAlertGoodNotFound {
                navigator.openSelectGoodScreen()
            }
        }
    }

    fun onClickMenu(){
        /*Выход в главное меню*/
    }

}