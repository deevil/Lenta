package com.lenta.bp12.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.TaskCreate
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


    val task = MutableLiveData<TaskCreate>()

    val good = MutableLiveData<Good>()

    val title = good.map { good ->
        good?.getNameWithMaterial()
    }

    val number = MutableLiveData("")

    val isCompactMode = good.map { good ->
        good?.kind == GoodKind.COMMON
    }

    val quantityType = good.map { good ->
        good?.kind?.let { type ->
            when {
                type == GoodKind.EXCISE && number.value?.length == Constants.EXCISE_68 -> "Партионно"
                type == GoodKind.EXCISE -> "Марочно"
                type == GoodKind.ALCOHOL -> "Партионно"
                else -> "Количество"
            }
        }
    }

    val markScanEnabled = good.map { good ->
        good?.kind == GoodKind.EXCISE
    }

    val quantity = good.map { good ->
        if (good?.isBox() == true) good.innerQuantity.dropZeros() else "1"
    }

    val quantityEnabled = good.map { good ->
        good?.isBox() == false
    }

    private val totalQuantity = quantity.map { quantity ->
        quantity?.toDoubleOrNull().sumWith(good.value?.quantity)
    }

    val totalWithUnits = totalQuantity.map { quantity ->
        "${quantity.dropZeros()} ${good.value?.units?.name}"
    }

    private val basket = good.map { good ->
        task.value?.let { task ->
            task.baskets.find {
                it.section == good?.section && it.type == good.type && it.control == good.control && it.provider == good.provider
            }
        }
    }

    val basketNumber = basket.map { basket ->
        val number = task.value?.baskets?.indexOf(basket) ?: -1
        if (number >= 0) number.toString() else ""
    }

    val basketQuantity = basket.map { basket ->
        "${task.value?.getQuantityByBasket(basket).sumWith(quantity.value?.toDoubleOrNull() ?: 0.0).dropZeros()} ${good.value?.units?.name}"
    }

    val totalTitle = MutableLiveData("Итого")

    val basketTitle = MutableLiveData("По корзине")

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
            when (good.kind) {
                GoodKind.COMMON -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                GoodKind.ALCOHOL -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                GoodKind.EXCISE -> quantity?.toDoubleOrNull() ?: 0.0 > 0
            }
        }
    }

    val detailsVisibility = good.map { good ->
        good?.kind == GoodKind.ALCOHOL || good?.kind == GoodKind.EXCISE
    }

    val rollbackVisibility = good.map { good ->
        good?.kind == GoodKind.EXCISE
    }

    val rollbackEnabled = MutableLiveData(false)

    // -----------------------------

    init {
        viewModelScope.launch {

            task.value = manager.task.value
            //good.value = manager.currentGood.value
            checkSearchNumber(manager.searchNumber)
        }
    }

    // -----------------------------

    private fun checkSearchNumber(number: String) {
        number.length.let { length ->
            if (length >= Constants.SAP_6) {
                when (length) {
                    Constants.SAP_6 -> getGoodByMaterial(number.takeLast(6))
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
            manager.currentGood.value = good
        } ?: loadGoodInfo(ean = ean)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { good ->
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
                        good.value = manager.currentGood.value

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
        good.value?.let { good ->
            good.isCounted = true
            good.quantity = totalQuantity.value ?: 0.0

            manager.currentGood.value = good
        }

        good.value?.let { good ->
            if (basket.value == null) {
                manager.addBasket(Basket(
                        section = good.section,
                        type = good.type,
                        control = good.control,
                        provider = good.provider
                ))
            }
        }

        manager.addGoodInTask()
    }


    fun onBackPressed() {
        navigator.showUnsavedDataWillBeLost {
            manager.searchFromList = false
            manager.searchNumber = ""
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
        good.value = manager.currentGood.value
    }

}