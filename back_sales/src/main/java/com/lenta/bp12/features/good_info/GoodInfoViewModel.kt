package com.lenta.bp12.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.GoodType
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.ExciseInfoNetRequest
import com.lenta.bp12.request.ExciseInfoParams
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.GoodInfoParams
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var exciseInfoNetRequest: ExciseInfoNetRequest


    val task by lazy {
        manager.task
    }

    val good by lazy {
        manager.currentGood
    }

    val title = good.map { good ->
        good?.getNameWithMaterial()
    }

    val number = MutableLiveData("")

    val isCompactMode = good.map { good ->
        good?.type == GoodType.COMMON
    }

    val quantityType = good.map { good ->
        good?.type?.let { type ->
            when {
                type == GoodType.EXCISE && number.value?.length == Constants.EXCISE_68 -> "Партионно"
                type == GoodType.EXCISE -> "Марочно"
                type == GoodType.ALCOHOL -> "Партионно"
                else -> "Количество"
            }
        }
    }

    val quantity = good.map { good ->
        if (good?.isBox() == true) good.innerQuantity.dropZeros() else "1"
    }

    val quantityEnabled = good.map { good ->
        good?.isBox() == false
    }

    val total = good.map { good ->
        good?.quantity.sumWith(quantity.value?.toDoubleOrNull())
    }

    val totalWithUnits = total.map { quantity ->
        "${quantity.dropZeros()} ${good.value?.units?.name}"
    }

    val basket = good.map { good ->
        "111 ${good?.units?.name}"
    }

    val totalTitle = MutableLiveData("Итого*")

    val basketTitle = MutableLiveData("*По корзине")

    val providers = good.map { good ->
        good?.providers?.let { providers ->
            val list = providers.toMutableList()
            if (list.size > 1) {
                list.add(0, ProviderInfo())
            }

            list.toList()
        }
    }

    val providerList = providers.map { list ->
        list?.map { it.name }
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

    val producers = good.map { good ->
        good?.producers?.let { producers ->
            val list = producers.toMutableList()
            if (list.size > 1) {
                list.add(0, ProducerInfo())
            }

            list.toList()
        }
    }

    val producerList = producers.map { list ->
        list?.map { it.name }
    }

    val producerEnabled = producers.map { producers ->
        producers?.size ?: 0 > 1
    }

    val producerPosition = MutableLiveData(0)

    val onSelectProducer = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            producerPosition.value = position
        }
    }

    val date = MutableLiveData("")

    val dateEnabled = MutableLiveData(true)


    val applyEnabled = quantity.map { quantity ->
        good.value?.let { good ->
            when (good.type) {
                GoodType.COMMON -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                GoodType.ALCOHOL -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                GoodType.EXCISE -> quantity?.toDoubleOrNull() ?: 0.0 > 0
            }
        }
    }

    val detailsVisibility = good.map { good ->
        good?.type == GoodType.ALCOHOL || good?.type == GoodType.EXCISE
    }

    val rollbackVisibility = good.map { good ->
        good?.type == GoodType.EXCISE
    }

    val rollbackEnabled = MutableLiveData(false)

    // -----------------------------

    init {
        viewModelScope.launch {
            checkSearchNumber(manager.searchNumber)
        }
    }

    // -----------------------------

    private fun checkSearchNumber(number: String) {
        number.length.let { length ->
            if (length >= Constants.SAP_6) {
                when (length) {
                    Constants.SAP_6 -> getGoodByMaterial("000000000000$number")
                    Constants.SAP_18 -> getGoodByMaterial(number)
                    Constants.SAP_OR_BAR_12 -> {
                        navigator.showTwelveCharactersEntered(
                                sapCallback = { getGoodByMaterial(number) },
                                barCallback = { getGoodByEan(number) }
                        )
                    }
                    Constants.EXCISE_68 -> { loadMarkInfo(number) }
                    Constants.EXCISE_150 -> { loadMarkInfo(number) }
                    else -> getGoodByEan(number)
                }
            }
        }
    }

    private fun getGoodByEan(ean: String) {
        manager.findGoodByEan(ean)?.let {  good ->
            manager.currentGood.value = good
        } ?: loadGoodInfo(ean = ean)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let {  good ->
            manager.currentGood.value = good
        } ?: loadGoodInfo(material = material)
    }

    private fun loadGoodInfo(ean: String? = null, material: String? = null) {
        require((ean != null) || (material != null)) {
            "At least one param must be not null - ean: $ean, material: $material"
        }

        viewModelScope.launch {
            navigator.showProgressLoadingData()

            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market ?: "Not found!",
                    ean = ean ?: "",
                    material = material ?: "",
                    taskType = task.value!!.type.type
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { goodInfo ->
                viewModelScope.launch {
                    if (manager.isGoodCanBeAdded(goodInfo)) {
                        manager.putInCurrentGood(goodInfo)
                    } else {
                        navigator.showNotMatchTaskSettingsAddingNotPossible {
                            if (manager.searchFromList) {
                                manager.searchFromList = false
                                manager.searchNumber = ""
                                navigator.goBack()
                                navigator.goBack()
                            } else {
                                navigator.goBack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadMarkInfo(number: String) {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            exciseInfoNetRequest(ExciseInfoParams(
                    tkNumber = sessionInfo.market ?: "Not found!",
                    material = good.value!!.material,
                    markNumber = number,
                    mode = 1,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { exciseInfo ->
                Logg.d { "--> exciseInfo = $exciseInfo" }
            }
        }
    }

    fun onScanResult(number: String) {
        if (applyEnabled.value!! && number.length >= Constants.SAP_6) {
            saveGoodInTask()

            manager.searchFromList = false
            manager.searchNumber = number
            checkSearchNumber(number)
        }
    }

    private fun saveGoodInTask() {
        total.value?.let {total ->
            manager.currentGood.value?.quantity = total
        }

        // Создать корзину, если есть необходимость
        // Сделать эту корзину текущей
        // ...

        manager.addGoodInTask()
    }


    fun onBackPressed() {
        navigator.showUnsavedDataWillBeLost {
            manager.searchFromList = false
            manager.searchNumber = ""
            navigator.goBack()
        }
    }

    fun onClickDetails() {
        navigator.openGoodDetailsScreen()
    }

    fun onClickApply() {
        saveGoodInTask()
        navigator.goBack()
        navigator.openBasketGoodListScreen()
    }

    fun addProvider() {
        navigator.openAddProviderScreen()
    }

    fun onClickRollback() {

    }

}