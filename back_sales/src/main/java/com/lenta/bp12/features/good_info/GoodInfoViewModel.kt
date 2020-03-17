package com.lenta.bp12.features.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.GoodType
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: ICreateTaskManager


    val good by lazy {
        manager.getCurrentGood()
    }

    val title = good.map {good ->
        good?.getNameWithMaterial()
    }

    val isCompactMode = good.map {good ->
        good?.type == GoodType.COMMON
    }

    val quantityType = good.map { good ->
        when (good?.type) {
            GoodType.EXCISE -> "Марочно"
            GoodType.ALCOHOL -> "Партионно"
            else -> "Количество"
        }
    }

    val quantity = good.map { good ->
        if (good?.isBox() == true) good.innerQuantity.dropZeros() else "1"
    }

    val quantityEnabled = good.map { good ->
        good?.isBox() == false
    }

    val total = good.map { good ->
        "${good?.quantity.sumWith(quantity.value?.toDoubleOrNull()).dropZeros()} ${good?.units?.name}"
    }

    val basket = good.map { good ->
        "${good?.quantity.sumWith(quantity.value?.toDoubleOrNull()).dropZeros()} ${good?.units?.name}"
    }

    val providerList = good.map { good ->
        good?.getPreparedProviderList()
    }

    val providerEnabled = providerList.map { providers ->
        providers?.size ?: 0 > 1
    }

    val providerPosition = MutableLiveData(0)

    val onSelectProvider = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            providerPosition.value = position
        }
    }







    val date = MutableLiveData("")

    val dateEnabled = MutableLiveData(true)

    val importerEnabled = MutableLiveData(true)

    val importerPosition = MutableLiveData(0)

    val onSelectImporter = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            importerPosition.value = position
        }
    }

    val importerList: MutableLiveData<List<String>> by lazy {
        MutableLiveData(List(3) {
            "Importer ${it + 1}"
        })
    }

    val rollbackVisibility = MutableLiveData(true)

    val detailsVisibility = MutableLiveData(true)

    val missingVisibility = MutableLiveData(true)

    val rollbackEnabled = MutableLiveData(false)

    val missingEnabled = MutableLiveData(false)

    val applyEnabled = MutableLiveData(true)

    // -----------------------------

    fun onClickRollback() {

    }

    fun onClickDetails() {

    }

    fun onClickMissing() {

    }

    fun onClickApply() {

    }

    fun addProvider() {

    }

}