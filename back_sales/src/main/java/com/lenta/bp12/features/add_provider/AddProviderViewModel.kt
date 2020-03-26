package com.lenta.bp12.features.add_provider

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.repository.DatabaseRepository
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddProviderViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var database: DatabaseRepository

    @Inject
    lateinit var manager: ICreateTaskManager


    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { good ->
            good?.getNameWithMaterial()
        }
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val provider = MutableLiveData<ProviderInfo>()

    val supplierName = provider.map { providerInfo ->
        providerInfo?.name
    }

    val applyEnabled = provider.map { providerInfo ->
        providerInfo != null
    }

    // -----------------------------

    override fun onOkInSoftKeyboard(): Boolean {
        numberField.value?.let { number ->
            searchProvider(number)
        }
        return true
    }

    private fun searchProvider(number: String) {
        viewModelScope.launch {
            database.getProviderInfo(number)?.let { providerInfo ->
                provider.value = providerInfo
            }
        }
    }

    fun onClickApply() {
        manager.addProviderInCurrentGood(provider.value!!)
        navigator.goBack()
    }

}