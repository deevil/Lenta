package com.lenta.bp14.features.print_settings

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class PrintSettingsViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    companion object {
        const val DATAMAX = 0
        const val ZEBRA = 1
    }

    val numberOfCopies: MutableLiveData<String> = MutableLiveData("1")
    val selectedPrinterType: MutableLiveData<Int> = MutableLiveData(0)
    val selectedPriceTagType: MutableLiveData<Int> = MutableLiveData(0)
    val ipAddressVisibility: MutableLiveData<Boolean> = MutableLiveData(false)
    val ipAddress: MutableLiveData<String> = MutableLiveData("10.254.25.12")

    fun increaseNumberOfCopies() {
        val copy = numberOfCopies.value?.toInt() ?: 0
        numberOfCopies.value = "" + (copy + 1)
    }

    fun reduceNumberOfCopies() {
        val copy = numberOfCopies.value?.toInt() ?: 0
        if (copy > 1) numberOfCopies.value = "" + (copy - 1)
    }

    override fun onClickPosition(position: Int) {
        selectedPrinterType.value = position

        when (position) {
            DATAMAX -> ipAddressVisibility.value = false
            ZEBRA -> ipAddressVisibility.value = true
        }
    }


}
