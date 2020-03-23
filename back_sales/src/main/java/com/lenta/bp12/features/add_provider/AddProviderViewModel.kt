package com.lenta.bp12.features.add_provider

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class AddProviderViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title by lazy {
        "000055 Гамак обыкновенный"
    }

    val applyEnabled = MutableLiveData(false)

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val supplierName = numberField.map {
        "ООО Кубань Север"
    }

    // -----------------------------

    fun onClickApply() {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }

}