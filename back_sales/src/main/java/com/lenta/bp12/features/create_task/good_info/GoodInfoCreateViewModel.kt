package com.lenta.bp12.features.create_task.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
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
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoCreateViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var markInfoNetRequest: MarkInfoNetRequest

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

    private val lastSuccessSearchNumber = MutableLiveData("")


    val isCompactMode by lazy {
        good.map { good ->
            good?.type == GoodType.COMMON
        }
    }

    private val scanModeType = MutableLiveData(ScanNumberType.DEFAULT)

    val accountingType by lazy {
        scanModeType.map { type ->
            when (type) {
                ScanNumberType.MARK_150, ScanNumberType.MARK_68 -> "Марочно"
                ScanNumberType.ALCOHOL, ScanNumberType.PART -> "Партионно"
                else -> "Количество"
            }
        }
    }

    val markScanEnabled by lazy {
        good.map { good ->
            good?.type == GoodType.EXCISE
        }
    }

    val totalTitle = MutableLiveData("Итого")

    val basketTitle = MutableLiveData("По корзине")

    var markInfoResult: MarkInfoResult? = null

    var mark: Mark? = null

    var part: Part? = null


    /**
    Ввод количества
     */

    val quantityField = MutableLiveData("1")

    val quantity = quantityField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    val quantityFieldEnabled by lazy {
        scanModeType.map { type ->
            when (type) {
                ScanNumberType.MARK_150, ScanNumberType.MARK_68 -> false
                else -> true
            }
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

    val isProviderSelected = providerEnabled.combineLatest(providerPosition).map {
        val isEnabled = it?.first ?: false
        val position = it?.second ?: 0

        isEnabled && position > 0 || !isEnabled && position == 0
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

    val isProducerSelected = producerEnabled.combineLatest(producerPosition).map {
        val isEnabled = it?.first ?: false
        val position = it?.second ?: 0

        isEnabled && position > 0 || !isEnabled && position == 0
    }

    /**
    Количество товара итого и по корзинам
     */

    private val totalQuantity by lazy {
        quantityField.map { quantity ->
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
        quantityField.combineLatest(basket).map {
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

    val dateEnabled = scanModeType.map { type ->
        when (type) {
            ScanNumberType.ALCOHOL, ScanNumberType.MARK_68 -> true
            else -> false
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val applyEnabled by lazy {
        scanModeType.combineLatest(quantity).combineLatest(isProviderSelected).combineLatest(isProducerSelected).combineLatest(isCorrectDate).map {
            it?.let {
                val type = it.first.first.first.first
                val quantity = it.first.first.first.second
                val isProviderSelected = it.first.first.second
                val isProducerSelected = it.first.second
                val isDateEntered = it.second

                when (type) {
                    ScanNumberType.COMMON -> quantity > 0.0 && isProviderSelected
                    ScanNumberType.ALCOHOL -> quantity > 0.0 && isProviderSelected && isProducerSelected && isDateEntered
                    ScanNumberType.EXCISE -> quantity > 0.0 && isProviderSelected && isProducerSelected && isDateEntered
                    ScanNumberType.MARK_150 -> isProviderSelected
                    ScanNumberType.MARK_68 -> isProviderSelected && isProducerSelected && isDateEntered
                    ScanNumberType.PART -> isProviderSelected && isProducerSelected
                    ScanNumberType.BOX -> true
                    else -> false
                }
            } ?: false
        }
    }

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
            Logg.d { "--> number length: $length" }
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
                        loadMarkInfo(number)
                    }
                    Constants.BOX -> {
                        loadBoxInfo(number)
                    }
                    else -> getGoodByEan(number)
                }
            }
        }
    }

    private fun getGoodByEan(ean: String) {
        manager.findGoodByEan(ean)?.let { foundGood ->
            setFoundGood(foundGood)
        } ?: loadGoodInfo(ean = ean)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { foundGood ->
            setFoundGood(foundGood)
        } ?: loadGoodInfo(material = material)
    }

    private fun setFoundGood(foundGood: Good) {
        good.value = foundGood
        lastSuccessSearchNumber.value = foundGood.material
        setScanModeFromGoodType(foundGood.type)
    }

    private fun setScanModeFromGoodType(goodType: GoodType) {
        scanModeType.value = when (goodType) {
            GoodType.COMMON -> ScanNumberType.COMMON
            GoodType.ALCOHOL -> ScanNumberType.ALCOHOL
            GoodType.EXCISE -> ScanNumberType.EXCISE
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
                setScanModeFromGoodType(good.type)
            }
        }
    }

    private fun loadMarkInfo(number: String) {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            markInfoNetRequest(MarkInfoParams(
                    tkNumber = sessionInfo.market ?: "Not found!",
                    material = good.value!!.material,
                    markNumber = number,
                    mode = 1,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleMarkLoadFailure) { markInfoResult ->
                Logg.d { "--> markInfoResult: $markInfoResult" }
                viewModelScope.launch {
                    markInfoResult.status.let { status ->
                        if (status == MarkStatus.OK.code || status == MarkStatus.BAD.code) {
                            addMarkInfo(number, markInfoResult)
                        } else if (status == MarkStatus.UNKNOWN.code) {

                            // todo Брать данные из 22 справочника с алкокодами

                             if (isCorrectPartAlcoCode(markInfoResult)) {
                                 if (isAlcoCodeBelongToCurrentGood(markInfoResult)) {
                                     addPartInfo(number, markInfoResult)
                                 } else {
                                     navigator.openAlertScreen("Алкокод не относится к этому товару")
                                 }
                             } else {
                                 navigator.openAlertScreen("Неизвестный алкокод")
                             }
                        } else {
                            navigator.openAlertScreen(markInfoResult.statusDescription)
                        }
                    }
                }
            }
        }
    }

    private fun isCorrectPartAlcoCode(markInfoResult: MarkInfoResult): Boolean {


        return false
    }

    private fun isAlcoCodeBelongToCurrentGood(markInfoResult: MarkInfoResult): Boolean {


        return false
    }

    private fun handleMarkLoadFailure(failure: Failure) {
        Logg.d { "--> handleMarkLoadFailure: $failure" }
        navigator.openAlertScreen(failure)
    }

    private fun addMarkInfo(number: String, markInfo: MarkInfoResult) {
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        markInfoResult = markInfo

        when (number.length) {
            Constants.EXCISE_150 -> {
                scanModeType.value = ScanNumberType.MARK_150
                updateProducers(markInfo.producers.toMutableList())
                date.value = markInfo.producedDate
            }
            Constants.EXCISE_68 -> {
                scanModeType.value = ScanNumberType.MARK_68
            }
        }
    }

    private fun addPartInfo(number: String, markInfo: MarkInfoResult) {
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        markInfoResult = markInfo
        scanModeType.value = ScanNumberType.PART
    }

    private fun loadBoxInfo(number: String) {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            markInfoNetRequest(MarkInfoParams(
                    tkNumber = sessionInfo.market ?: "Not found!",
                    material = good.value!!.material,
                    markNumber = number,
                    mode = 2,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleBoxLoadFailure) { markInfoResult ->
                Logg.d { "--> exciseInfoResult: $markInfoResult" }
                viewModelScope.launch {
                    markInfoResult.status.let { status ->
                        if (status == BoxStatus.OK.code) {
                            addBoxInfo(number, markInfoResult)
                        } else {
                            navigator.openAlertScreen(markInfoResult.statusDescription)
                        }
                    }
                }
            }
        }
    }

    private fun handleBoxLoadFailure(failure: Failure) {
        Logg.d { "--> handleMarkLoadFailure: $failure" }
        navigator.openAlertScreen(failure)
    }

    private fun addBoxInfo(number: String, markInfo: MarkInfoResult) {
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        markInfoResult = markInfo
        scanModeType.value = ScanNumberType.BOX
    }


    private fun updateProviders(providers: MutableList<ProviderInfo>) {
        sourceProviders.value = providers
    }

    private fun updateProducers(producers: MutableList<ProducerInfo>) {
        sourceProducers.value = producers
    }

    private fun saveChanges() {
        if (applyEnabled.value!!) {

        }
    }

    private fun addCommonPosition() {
        good.value?.let { good ->
            good.addPosition(quantity.value!!, getProvider(), date.value)

            if (basket.value == null) {
                addBasket()
            }
        }
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