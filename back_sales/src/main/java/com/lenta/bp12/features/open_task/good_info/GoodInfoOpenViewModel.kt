package com.lenta.bp12.features.open_task.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.Category
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.ExciseInfoNetRequest
import com.lenta.bp12.request.ExciseInfoParams
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.GoodInfoParams
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoOpenViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: IOpenTaskManager

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

    val position by lazy {
        manager.currentPosition
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

    val category = MutableLiveData(Category.QUANTITY)

    val markScanEnabled by lazy {
        good.map { good ->
            good?.kind == GoodKind.EXCISE
        }
    }

    val quantity by lazy {
        position.map { position ->
            if (position?.isCounted == true) {
                position.quantity.dropZeros()
            } else {
                if (position?.isBox() == true) position.innerQuantity.dropZeros() else "1"
            }
        }
    }

    val quantityEnabled = MutableLiveData(true)

    private val totalQuantity by lazy {
        quantity.map { quantity ->
            quantity?.toDoubleOrNull().sumWith(good.value?.getTotalQuantity())
        }
    }

    val totalWithUnits by lazy {
        totalQuantity.combineLatest(good).map {
            val quantity = it!!.first
            val good = it.second

            "${quantity.dropZeros()} ${good.units.name}"
        }
    }

    val totalTitle = MutableLiveData("Итого")

    private val provider by lazy {
        task.map { task ->
            task?.provider
        }
    }

    val providerName by lazy {
        provider.map { provider ->
            provider?.name
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
        quantity.combineLatest(good).map {
            val quantity = it!!.first
            val good = it.second

            when (good.kind) {
                GoodKind.COMMON -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                GoodKind.ALCOHOL -> quantity?.toDoubleOrNull() ?: 0.0 > 0
                GoodKind.EXCISE -> quantity?.toDoubleOrNull() ?: 0.0 > 0
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

    private var isExistUnsavedData = false

    // -----------------------------

    init {
        viewModelScope.launch {
            if (position.value != null) {
                category.value = position.value!!.category
            } else {
                checkSearchNumber(manager.searchNumber)
            }
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
        good.value?.let { good ->
            if (good.ean == ean) {
                return
            }
        }

        val good = manager.findGoodByEan(ean)
        if (good != null && good.isFullData) {
            manager.updateCurrentGood(good)
        } else {
            loadGoodInfo(ean = ean)
        }
    }

    private fun getGoodByMaterial(material: String) {
        good.value?.let { good ->
            if (good.isSameMaterial(material)) {
                return
            }
        }

        val good = manager.findGoodByMaterial(material)
        if (good != null && good.isFullData) {
            manager.updateCurrentGood(good)
        } else {
            loadGoodInfo(material = material)
        }
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
                    taskType = task.value!!.properties!!.type
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { goodInfo ->
                viewModelScope.launch {
                    if (manager.isGoodCanBeAdded(goodInfo)) {
                        isExistUnsavedData = true
                        manager.currentPosition.value = null
                        manager.putInCurrentGood(goodInfo)
                        category.value = Category.QUANTITY
                    } else {
                        navigator.showNotMatchTaskSettingsAddingNotPossible {
                            navigator.goBack()
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

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.goBack()
        navigator.openAlertScreen(failure)
    }

    fun onScanResult(number: String) {
        if (!task.value!!.isStrict && applyEnabled.value!! && number.length >= Constants.SAP_6) {
            saveGoodInTask()

            manager.searchNumber = number
            checkSearchNumber(number)
        }
    }

    fun updateData() {
        manager.updateCurrentGood(good.value)
    }

    fun onClickRollback() {

    }

    fun onClickDetails() {
        navigator.openGoodDetailsOpenScreen()
    }

    fun onClickMissing() {
        quantity.value = "0"
        saveGoodInTask()
        navigator.goBack()
    }

    fun onClickApply() {
        saveGoodInTask()
        navigator.goBack()
    }

    private fun saveGoodInTask() {
        good.value?.let { good ->
            val quantity = quantity.value?.toDoubleOrNull() ?: 0.0

            if (position.value?.isCounted == true) {
                good.replacePosition(quantity, task.value!!.provider, category.value!!)
            } else {
                good.addPosition(quantity, task.value!!.provider, category.value!!)
            }

            manager.updateCurrentGood(good)
        }

        manager.addCurrentGoodInTask()
    }

    fun onBackPressed() {
        if (isExistUnsavedData) {
            navigator.showUnsavedDataWillBeLost {
                manager.currentPosition.value = null
                manager.searchNumber = ""
                navigator.goBack()
            }
        } else {
            manager.currentPosition.value = null
            manager.searchNumber = ""
            navigator.goBack()
        }
    }

}