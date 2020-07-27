package com.lenta.shared.features.weight_equipment_name

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class WeightEquipmentNameViewModel : CoreViewModel() {

    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var navigator: ICoreNavigator

    val weightEquipmentNameField = MutableLiveData("")

    val applyButtonEnabled = weightEquipmentNameField.map {
        it?.isNotEmpty()
    }

    init {
        launchUITryCatch {
            weightEquipmentNameField.value = appSettings.weightEquipmentName
        }
    }

    fun onClickApply() {
        appSettings.weightEquipmentName = weightEquipmentNameField.value
        navigator.goBack()
    }

}
