package com.lenta.bp12.features.create_task.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.MarkStatus
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.Good
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.*
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoCreateNViewModel : CoreViewModel() {

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

    @Inject
    lateinit var database: IDatabaseRepository


    /**
    Переменные
     */

    val task by lazy {
        manager.currentTask
    }

    val good = MutableLiveData<Good>()

    val title by lazy {
        good.map { good ->
            good?.getNameWithMaterial()
        }
    }

    private var isExistUnsavedData = false

    val lastSuccessSearchNumber = MutableLiveData("")

    var exciseInfo: ExciseInfoResult? = null

    val isCompactMode by lazy {
        good.map { good ->
            good?.type == GoodKind.COMMON
        }
    }

    /*val quantityType by lazy {
        good.map { good ->
            good?.type?.let { type ->
                when {
                    type == GoodKind.EXCISE && searchNumber.length == Constants.EXCISE_68 -> "Партионно"
                    type == GoodKind.EXCISE -> "Марочно"
                    type == GoodKind.ALCOHOL -> "Партионно"
                    else -> "Количество"
                }
            }
        }
    }*/

    val markScanEnabled by lazy {
        good.map { good ->
            good?.type == GoodKind.EXCISE
        }
    }

    val totalTitle = MutableLiveData("Итого")

    val basketTitle = MutableLiveData("По корзине")


    var lastScannedMark: Mark? = null


    /**
    Ввод количества
     */

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

    /**
    Список поставщиков
     */

    private val sourceProviders = MutableLiveData(mutableListOf<ProviderInfo>())

    private val providers = sourceProviders.map {
        it?.let { providers ->
            val list = providers.toMutableList()
            if (list.size > 1) {
                list.add(0, ProviderInfo())
            }

            list.toList()
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

    /**
    Список производителей
     */

    private val sourceProducers = MutableLiveData(mutableListOf<ProducerInfo>())

    private val producers = sourceProducers.map {
        it?.let { producers ->
            val list = producers.toMutableList()
            if (list.size > 1) {
                list.add(0, ProducerInfo())
            }

            list.toList()
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

    /**
    Количество товара итого и по корзинам
     */

    private val totalQuantity by lazy {
        quantity.map { quantity ->
            quantity?.toDoubleOrNull().sumWith(good.value?.getTotalQuantity())
        }
    }

    val totalWithUnits by lazy {
        totalQuantity.map { quantity ->
            "${quantity.dropZeros()} ${good.value?.units?.name}"
        }
    }

    private val basket by lazy {
        good.combineLatest(providerPosition).map {
            it?.first?.let { good ->
                task.value?.let { task ->
                    task.baskets.find { basket ->
                        basket.section == good.section && basket.matype == good.matype && basket.control == good.control && basket.provider == getProvider()
                    }
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
        quantity.combineLatest(basket).map {
            it?.first?.let { quantity ->
                "${task.value?.getQuantityByBasket(basket.value).sumWith(quantity.toDoubleOrNull() ?: 0.0).dropZeros()} ${good.value?.units?.name}"
            }
        }
    }

    /**
    Дата производства
     */

    val date = MutableLiveData("")

    private val isCorrectDate = date.map { date ->
        date?.length ?: 0 == 10
    }

    val dateEnabled = MutableLiveData(true)

    /**
    Кнопки нижнего тулбара
     */

    val applyEnabled by lazy {
        quantity.combineLatest(isCorrectDate).map {
            val quantity = it?.first?.toDoubleOrNull() ?: 0.0
            val isCorrectDate = it?.second ?: false

            good.value?.let { good ->
                when (good.type) {
                    GoodKind.COMMON -> quantity > 0
                    GoodKind.ALCOHOL -> quantity > 0 && isCorrectDate
                    GoodKind.EXCISE -> quantity > 0 && isCorrectDate
                }
            }
        }
    }

    val detailsVisibility = MutableLiveData(true)

    val rollbackVisibility = MutableLiveData(true)

    val rollbackEnabled = MutableLiveData(true)

    /**
    Блок инициализации
     */

    init {
        viewModelScope.launch {
            checkSearchNumber(manager.searchNumber)
        }
    }

    /**
    Методы
     */

    fun onScanResult(number: String) {
        if (number.length >= Constants.SAP_6) {
            if (applyEnabled.value!!) {
                saveChanges()
            }

            manager.openGoodFromList = false
            manager.searchNumber = number
            checkSearchNumber(number)
        }
    }

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
                    Constants.EXCISE_150 -> {
                        loadMarkInfo(number)
                    }
                    Constants.EXCISE_68 -> {
                        //loadMarkInfo(number)
                    }
                    else -> getGoodByEan(number)
                }
            }
        }
    }

    private fun getGoodByEan(ean: String) {
        manager.findGoodByEan(ean)?.let { foundGood ->
            good.value = foundGood
            lastSuccessSearchNumber.value = foundGood.material
        } ?: loadGoodInfo(ean = ean)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { foundGood ->
            good.value = foundGood
            lastSuccessSearchNumber.value = foundGood.material
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
                    taskType = task.value!!.properties!!.type
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { goodInfo ->
                viewModelScope.launch {
                    if (manager.isGoodCanBeAdded(goodInfo)) {
                        isExistUnsavedData = true
                        addGood(goodInfo)
                    } else {
                        navigator.showNotMatchTaskSettingsAddingNotPossible {
                            if (manager.openGoodFromList) {
                                manager.openGoodFromList = false
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

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        if (manager.openGoodFromList) {
            manager.openGoodFromList = false
            manager.searchNumber = ""
            navigator.goBack()
        }

        navigator.openAlertScreen(failure)
    }

    private fun addGood(goodInfo: GoodInfoResult) {
        viewModelScope.launch {
            good.value = Good(
                    ean = goodInfo.eanInfo.ean,
                    material = goodInfo.materialInfo.material,
                    name = goodInfo.materialInfo.name,
                    units = database.getUnitsByCode(goodInfo.materialInfo.unitsCode),
                    type = goodInfo.getGoodType(),
                    matype = goodInfo.materialInfo.matype,
                    control = goodInfo.getControlType(),
                    section = goodInfo.materialInfo.section,
                    matrix = getMatrixType(goodInfo.materialInfo.matrix),
                    innerQuantity = goodInfo.materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0,
                    orderUnits = database.getUnitsByCode(goodInfo.materialInfo.orderUnitCode),
                    providers = goodInfo.providers.toMutableList(),
                    producers = goodInfo.producers.toMutableList()
            )

            good.value?.let { good ->
                lastSuccessSearchNumber.value = good.material
                updateProviders(good.providers)
                updateProducers(good.producers)
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
            }.either(::handleFailure) { exciseInfoResult ->
                viewModelScope.launch {
                    exciseInfoResult.status.let { status ->
                        if (status == MarkStatus.OK.code || status == MarkStatus.BAD.code) {
                            lastSuccessSearchNumber.value = manager.searchNumber
                            exciseInfo = exciseInfoResult

                            // todo Наверно здесь лучше всего обновить количество, дату и список производителей
                            // ...

                        } else {
                            navigator.openAlertScreen(exciseInfoResult.statusDescription)
                        }
                    }
                }
            }
        }
    }

    private fun updateProviders(providers: MutableList<ProviderInfo>) {
        sourceProviders.value = providers
    }

    private fun updateProducers(producers: MutableList<ProducerInfo>) {
        sourceProducers.value = producers
    }

    private fun saveChanges() {
        if (applyEnabled.value!!) {
            if (isPosition()) {
                addCommonPosition()
            } else if (isMark()) {
                addMarkPosition()
            }
        }
    }

    private fun addCommonPosition() {
        good.value?.let { good ->
            val quantity = quantity.value?.toDoubleOrNull() ?: 0.0
            good.addPosition(quantity, getProvider(), date.value)

            if (basket.value == null) {
                addBasket()
            }
        }
    }

    private fun addMarkPosition() {
        /*good.value?.let { good ->
            val quantity = quantity.value?.toDoubleOrNull() ?: 0.0
            good.addPosition(quantity, getProvider(), date.value)

            if (basket.value == null) {
                addBasket()
            }
        }*/
    }

    private fun addBasket() {
        good.value?.let { good ->
            manager.addBasket(Basket(
                    section = good.section,
                    matype = good.matype,
                    control = good.control,
                    provider = getProvider()
            ))
        }
    }

    private fun getProvider(): ProviderInfo? {
        val position = providerPosition.value!!
        return providers.value?.let { providers ->
            when (providers.size) {
                0 -> null
                1 -> providers[0]
                else -> if (position != 0) providers[position] else null
            }
        }
    }


    /**
    Различные проверки
     */

    private fun isPosition(): Boolean {
        return manager.searchNumber.length < Constants.EXCISE_68
    }

    private fun isMark(): Boolean {
        manager.searchNumber.length.let { length ->
            return length == Constants.EXCISE_68 || length == Constants.EXCISE_150
        }
    }


    private fun addScannedMark(isBadMark: Boolean = false) {
        good.value?.let { good ->
            /*marks.value?.add(Mark(
                    material = good.material,
                    markNumber = lastScannedNumber.value!!,

            ))*/
        }


    }

    private fun saveExcise150() {
        good.value?.let { good ->

        }
    }


    fun updateData() {
        manager.updateCurrentGood(good.value)
    }


    /**
    Обработка нажатий кнопок
     */

    fun onBackPressed() {
        if (isExistUnsavedData) {
            navigator.showUnsavedDataWillBeLost {
                manager.openGoodFromList = false
                manager.searchNumber = ""
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
    }

    fun onClickApply() {
        saveChanges()
        manager.addOrUpdateGood(good.value!!)
        isExistUnsavedData = false

        navigator.goBack()
        navigator.openBasketGoodListScreen()
    }

    fun onClickDetails() {
        navigator.openGoodDetailsCreateScreen()
    }

    fun addProvider() {
        navigator.openAddProviderScreen()
    }

    fun onClickRollback() {

    }

}