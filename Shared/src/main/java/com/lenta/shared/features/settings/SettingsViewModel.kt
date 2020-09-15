package com.lenta.shared.features.settings

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.PackageName
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class SettingsViewModel : CoreViewModel(){

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var screenNavigator: ICoreNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    var isMainMenu = MutableLiveData(true)

    var selectPrinterButtonVisibility = MutableLiveData(true)

    var selectOperModeButtonVisibility = MutableLiveData(true)

    var changeWeightEquipmentButtonVisibility = MutableLiveData(false)

    var updateAppButtonVisibility = MutableLiveData(true)

    var changeLabelPrinterVisibility = MutableLiveData(false)

    init {
        launchUITryCatch {
            when (sessionInfo.packageName) {
                PackageName.PLE.path -> {
                    selectPrinterButtonVisibility.value = false
                    selectOperModeButtonVisibility.value = false
                }
                PackageName.PRO.path -> {
                    changeWeightEquipmentButtonVisibility.value = true
                }
                PackageName.OPP.path -> {
                    selectPrinterButtonVisibility.value = false
                    updateAppButtonVisibility.value = false
                }
                PackageName.GRZ.path -> {
                    changeLabelPrinterVisibility.value = true
                }
            }
        }
    }

    fun onClickBack() {
        screenNavigator.goBack()
    }

    fun onClickChangeWeightEquipment() {
        screenNavigator.openWeightEquipmentNameScreen()
    }

    fun onClickPrinter() {
        when (sessionInfo.packageName) {
            PackageName.PRO.path -> screenNavigator.openEnterPrinterAddressScreen()
            else -> screenNavigator.openPrinterChangeScreen()
        }
    }

    fun onClickPrinterLabel() {
        screenNavigator.openEnterPrinterAddressScreen()
    }

    fun onClickWork() {
        screenNavigator.openSelectOperModeScreen()
    }

    fun onClickTechLog() {
        screenNavigator.openTechLoginScreen()
    }

    fun onClickUpdateApp() {
        screenNavigator.openUpdateAppScreen()
    }

}