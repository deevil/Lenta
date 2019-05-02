package com.lenta.bp10.features.printer_change

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.printer_change.CorePrinterChangeViewModel
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class PrinterChangeViewModel : CorePrinterChangeViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val selectedPosition: MutableLiveData<Int> = MutableLiveData()

    override fun onClickBack() {
        screenNavigator.goBack()
    }

    override fun onClickApp() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickApp")
    }

    override fun onBackPressed() {
    }

    override fun onClickPosition(position: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        selectedPosition.value = position
    }

}
