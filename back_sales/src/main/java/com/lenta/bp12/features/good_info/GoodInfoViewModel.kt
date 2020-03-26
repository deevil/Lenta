package com.lenta.bp12.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.Basket
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
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var exciseInfoNetRequest: ExciseInfoNetRequest


    val task by lazy {
        manager.currentTask
    }

    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { good ->
            good?.getNameWithMaterial()
        }
    }

    val number = MutableLiveData("")

    val isCompactMode by lazy {
        good.map { good ->
            good?.kind == GoodKind.COMMON
        }
    }

    val quantityType by lazy {
        good.map { good ->
            good?.kind?.let { type ->
                when {
                    type == GoodKind.EXCISE && number.value?.length == Constants.EXCISE_68 -> "Партионно"
                    type == GoodKind.EXCISE -> "Марочно"
                    type == GoodKind.ALCOHOL -> "Партионно"
                    else -> "Количество"
                }
            }
        }
    }

    val markScanEnabled by lazy {
        good.map { good ->
            good?.kind == GoodKind.EXCISE
        }
    }

    val quantity by lazy {
        good.map { good ->
            if (good?.isBox() == true) good.innerQuantity.dropZeros() else "1"
        }
    }

    val quantityEnabled by lazy {
        good.map { good ->
            good?.isBox() == false
        }
    }

    private val totalQuantity by lazy {
        quantity.map { quantity ->
            quantity?.toDoubleOrNull().sumWith(good.value?.quantity)
        }
    }

    val totalWithUnits by lazy {
        totalQuantity.map { quantity ->
            "${quantity.dropZeros()} ${good.value?.units?.name}"
        }
    }

    private val basket by lazy {
        good.map { good ->
            task.value?.let { task ->
                task.baskets.find {
                    it.section == good?.section && it.type == good.type && it.control == good.control && it.provider == good.provider
                }
            }
        }
    }

    val basketNumber by lazy {
        basket.map { basket ->
            val number = task.value?.baskets?.indexOf(basket) ?: -1
            if (number >= 0) "${number + 1}" else ""
        }
    }

    val basketQuantity by lazy {
        quantity.map { quantity ->
            "${task.value?.getQuantityByBasket(basket.value).sumWith(quantity?.toDoubleOrNull() ?: 0.0).dropZeros()} ${good.value?.units?.name}"
        }
    }

    val totalTitle = MutableLiveData("Итого")

    val basketTitle = MutableLiveData("По корзине")

    private val providers by lazy {
        good.map { good ->
            good?.providers?.let { providers ->
                val list = providers.toMutableList()
                if (list.size > 1) {
                    list.add(0, ProviderInfo())
                }

                list.toList()
            }
        }
    }

    val providerList by lazy {
        providers.map { list ->
            list?.map { it.name }
        }
    }

    val providerEnabled by lazy {
        providerList.map { providers ->
            providers?.size ?: 0 > 1
        }
    }

    val providerPosition = MutableLiveData(0)

    val onSelectProvider = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            providerPosition.value = position
        }
    }

    private val producers by lazy {
        good.map { good ->
            good?.producers?.let { producers ->
                val list = producers.toMutableList()
                if (list.size > 1) {
                    list.add(0, ProducerInfo())
                }

                list.toList()
            }
        }
    }

    val producerList by lazy {
        producers.map { list ->
            list?.map { it.name }
        }
    }

    val producerEnabled by lazy {
        producers.map { producers ->
            producers?.size ?: 0 > 1
        }
    }

    val producerPosition = MutableLiveData(0)

    val onSelectProducer = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            producerPosition.value = position
        }
    }

    val date = MutableLiveData("")

    val dateEnabled = MutableLiveData(true)


    val applyEnabled by lazy {
        quantity.map { quantity ->
            good.value?.let { good ->
                when (good.kind) {
                    GoodKind.COMMON -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                    GoodKind.ALCOHOL -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                    GoodKind.EXCISE -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                }
            }
        }
    }

    val detailsVisibility by lazy {
        good.map { good ->
            good?.kind == GoodKind.ALCOHOL || good?.kind == GoodKind.EXCISE
        }
    }

    val rollbackVisibility by lazy {
        good.map { good ->
            good?.kind == GoodKind.EXCISE
        }
    }

    val rollbackEnabled = MutableLiveData(false)

    var isExistUnsavedData = false

    // -----------------------------

    init {
        viewModelScope.launch {
            //task.value = manager.task.value
            checkSearchNumber(manager.searchNumber)
        }
    }

    // -----------------------------

    private fun checkSearchNumber(number: String) {
        number.length.let { length ->
            if (length >= Constants.SAP_6) {
                when (length) {
                    Constants.SAP_6 -> getGoodByMaterial(number)
                    Constants.SAP_18 -> getGoodByMaterial(number)
                    Constants.SAP_OR_BAR_12 -> {
                        navigator.showTwelveCharactersEntered(
                                sapCallback = { getGoodByMaterial(number) },
                                barCallback = { getGoodByEan(number) }
                        )
                    }
                    Constants.EXCISE_68 -> {
                        //loadMarkInfo(number)
                    }
                    Constants.EXCISE_150 -> {
                        //loadMarkInfo(number)
                    }
                    else -> getGoodByEan(number)
                }
            }
        }
    }

    private fun getGoodByEan(ean: String) {
        manager.findGoodByEan(ean)?.let { good ->
            Logg.d { "--> getGoodByEan: good = $good" }
            manager.updateCurrentGood(good)
        } ?: loadGoodInfo(ean = ean)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { good ->
            Logg.d { "--> getGoodByMaterial: good = $good" }
            manager.updateCurrentGood(good)
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
                        isExistUnsavedData = true
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
                viewModelScope.launch {
                    Logg.d { "--> exciseInfo = $exciseInfo" }

                    // todo Логика сохранения марок
                    isExistUnsavedData = true

                }
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
        good.value?.let { good ->
            good.isCounted = true
            good.quantity = totalQuantity.value ?: 0.0
            good.provider = providers.value!![providerPosition.value!!]

            Logg.d { "--> saveGoodInTask: good = $good" }

            manager.updateCurrentGood(good)

            if (basket.value == null) {
                manager.addBasket(Basket(
                        section = good.section,
                        type = good.type,
                        control = good.control,
                        provider = good.provider
                ))
            }
        }

        manager.addCurrentGoodInTask()
    }

    fun onBackPressed() {
        if (isExistUnsavedData) {
            navigator.showUnsavedDataWillBeLost {
                manager.searchFromList = false
                manager.searchNumber = ""
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
    }

    fun onClickApply() {
        saveGoodInTask()
        navigator.goBack()
        navigator.openBasketGoodListScreen()
    }

    fun onClickDetails() {
        navigator.openGoodDetailsScreen()
    }

    fun addProvider() {
        navigator.openAddProviderScreen()
    }

    fun onClickRollback() {

    }

    fun updateData() {
        manager.updateCurrentGood(good.value)
    }

}