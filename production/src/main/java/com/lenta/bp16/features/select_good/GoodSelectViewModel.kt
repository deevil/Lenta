package com.lenta.bp16.features.select_good

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import dagger.multibindings.IntKey
import javax.inject.Inject

class GoodSelectViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: DatabaseRepository

    val deviceIp = MutableLiveData("")

    val enteredEanField = MutableLiveData("")
    val requestFocusEnteredEanField = MutableLiveData(true)

    fun onClickNext(){
        launchUITryCatch {
            val goodEan = database.getGoodByEan(enteredEanField.value.toString())
            val material = goodEan?.material.orEmpty()
            when(goodEan){
                null -> navigator.showAlertGoodNotFound {
                    navigator.openSelectGoodScreen()
                }
                else -> navigator.openGoodInfoScreen()
            }
        }
    }

    fun onClickMenu(){
        /*Выход в главное меню*/
    }

}