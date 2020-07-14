package com.lenta.bp12.features.create_task.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.*
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.getFormattedDate
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.math.BigInteger
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

    @Inject
    lateinit var resource: IResourceManager


    /**
    Переменные
     */

    val task by lazy {
        manager.currentTask
    }

    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { good ->
            good?.getNameWithMaterial() ?: task.value?.getFormattedName()
        }
    }

    private val lastSuccessSearchNumber = MutableLiveData("")

    val isCompactMode by lazy {
        good.map { good ->
            good?.kind == GoodKind.COMMON
        }
    }

    private val scanModeType = MutableLiveData(ScanNumberType.DEFAULT)

    val accountingType by lazy {
        scanModeType.map { type ->
            when (type) {
                ScanNumberType.MARK_150, ScanNumberType.MARK_68 -> resource.typeMark()
                ScanNumberType.ALCOHOL, ScanNumberType.PART -> resource.typePart()
                else -> resource.typeQuantity()
            }
        }
    }

    val markScanEnabled by lazy {
        good.map { good ->
            good?.kind == GoodKind.EXCISE
        }
    }

    private val markInfoResult = MutableLiveData<MarkInfoResult>()

    private var scanCodeInfo: ScanCodeInfo? = null

    private var isExistUnsavedData = false

    private var isEanLastScanned = false

    private var thereWasRollback = false

    /**
    Ввод количества
     */

    val quantityField = MutableLiveData("0")

    val quantity = quantityField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    val quantityFieldEnabled by lazy {
        scanModeType.map { type ->
            when (type) {
                ScanNumberType.EXCISE, ScanNumberType.MARK_150, ScanNumberType.MARK_68, ScanNumberType.BOX -> false
                else -> true
            }
        }
    }

    /**
    Количество товара итого
     */

    val totalTitle by lazy {
        good.map { good ->
            resource.totalWithConvertingInfo(good?.convertingInfo.orEmpty())
        }
    }

    private val totalQuantity by lazy {
        good.combineLatest(quantity).map {
            it?.let {
                val total = it.first.getTotalQuantity()
                val current = it.second

                total.sumWith(current)
            }
        }
    }

    val totalWithUnits by lazy {
        totalQuantity.map { quantity ->
            "${quantity.dropZeros()} ${good.value?.commonUnits?.name}"
        }
    }

    /**
    Количество товара по корзинам
     */

    val basketTitle by lazy {
        MutableLiveData(resource.byBasket())
    }

    val basketNumber by lazy {
        good.combineLatest(quantity).combineLatest(isProviderSelected).map {
            it?.let {
                val isProviderSelected = it.second

                if (isProviderSelected) {
                    task.value?.let { task ->
                        getBasket()?.let { basket ->
                            "${task.baskets.indexOf(basket) + 1}"
                        } ?: "${task.baskets.size + 1}"
                    }.orEmpty()
                } else ""
            }
        }
    }

    val basketQuantity by lazy {
        good.combineLatest(quantity).combineLatest(isProviderSelected).map {
            it?.let {
                val good = it.first.first
                val quantity = it.first.second
                val isProviderSelected = it.second

                val units = good.commonUnits.name

                if (isProviderSelected) {
                    getBasket()?.let { basket ->
                        "${good.getQuantityByProvider(basket.provider.code).sumWith(quantity).dropZeros()} $units"
                    } ?: "${quantity.dropZeros()} $units"
                } else {
                    "0 $units"
                }
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
                list.add(0, ProviderInfo(name = resource.chooseProvider()))
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

    private val isProviderSelected = providerEnabled.combineLatest(providerPosition).map {
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
                list.add(0, ProducerInfo(name = resource.chooseProducer()))
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

    private val isProducerSelected = producerEnabled.combineLatest(producerPosition).map {
        val isEnabled = it?.first ?: false
        val position = it?.second ?: 0

        isEnabled && position > 0 || !isEnabled && position == 0
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
            ScanNumberType.ALCOHOL, ScanNumberType.PART -> true
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
                val isEnteredQuantity = it.first.first.first.second > 0.0
                val isProviderSelected = it.first.first.second
                val isProducerSelected = it.first.second
                val isDateEntered = it.second

                when (type) {
                    ScanNumberType.COMMON -> isEnteredQuantity && isProviderSelected
                    ScanNumberType.ALCOHOL -> isEnteredQuantity && isProviderSelected && isProducerSelected && isDateEntered
                    ScanNumberType.EXCISE -> false
                    ScanNumberType.MARK_150 -> isEnteredQuantity && isProviderSelected
                    ScanNumberType.MARK_68 -> isEnteredQuantity && isProviderSelected && isProducerSelected
                    ScanNumberType.PART -> isEnteredQuantity && isProviderSelected && isProducerSelected && isDateEntered
                    ScanNumberType.BOX -> isEnteredQuantity && isProviderSelected && isProducerSelected
                    else -> false
                }
            } ?: false
        }
    }

    val rollbackVisibility = scanModeType.map { type ->
        when (type) {
            ScanNumberType.MARK_150, ScanNumberType.MARK_68, ScanNumberType.BOX -> true
            else -> false
        }
    }

    val rollbackEnabled = markInfoResult.map { info ->
        info != null
    }

    /**
    Блок инициализации
     */

    init {
        viewModelScope.launch {
            manager.clearCurrentGood()
            checkSearchNumber(manager.searchNumber)
        }
    }

    /**
    Методы
     */

    fun onScanResult(number: String) {
        if (number.length < Constants.SAP_6 || number.length > Constants.MARK_150) {
            return
        }

        if (applyEnabled.value!! || good.value!!.kind == GoodKind.EXCISE &&
                (number.length == Constants.MARK_150 || number.length == Constants.MARK_68 || number.length == Constants.BOX_26)) {
            if (!thereWasRollback) {
                saveChanges()
            } else {
                thereWasRollback = false
            }

            manager.searchGoodFromList = false
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
                    Constants.MARK_150 -> loadMarkInfo(number)
                    Constants.MARK_68 -> loadMarkInfo(number)
                    Constants.BOX_26 -> loadBoxInfo(number)
                    else -> getGoodByEan(number)
                }
            }
        }
    }

    private fun getGoodByEan(ean: String) {
        isEanLastScanned = true
        scanCodeInfo = ScanCodeInfo(ean)
        val eanWithoutWeight = scanCodeInfo?.eanWithoutWeight ?: ean

        manager.findGoodByEan(eanWithoutWeight)?.let { foundGood ->
            lastSuccessSearchNumber.value = eanWithoutWeight
            setFoundGood(foundGood)
        } ?: loadGoodInfo(ean = eanWithoutWeight)
    }

    private fun getGoodByMaterial(material: String) {
        isEanLastScanned = false
        manager.findGoodByMaterial(material)?.let { foundGood ->
            lastSuccessSearchNumber.value = material
            setFoundGood(foundGood)
        } ?: loadGoodInfo(material = material)
    }

    private fun setFoundGood(foundGood: GoodCreate) {
        manager.updateCurrentGood(foundGood)
        setScanModeFromGoodKind(foundGood.kind)
        updateProviders(foundGood.providers)
        updateProducers(foundGood.producers)
        setDefaultQuantity(foundGood)
    }

    private fun setDefaultQuantity(good: GoodCreate) {
        if (good.kind == GoodKind.COMMON) {
            if (good.commonUnits == Uom.KG) {
                quantityField.value = (scanCodeInfo?.getQuantity(good.convertingUnits)
                        ?: 0.0).dropZeros()
            } else {
                if (isEanLastScanned) {
                    quantityField.value = "1"
                }
            }
        }
    }

    private fun setScanModeFromGoodKind(goodKind: GoodKind) {
        scanModeType.value = when (goodKind) {
            GoodKind.COMMON -> ScanNumberType.COMMON
            GoodKind.ALCOHOL -> ScanNumberType.ALCOHOL
            GoodKind.EXCISE -> ScanNumberType.EXCISE
        }
    }

    private fun loadGoodInfo(ean: String? = null, material: String? = null) {
        require((ean != null) || (material != null)) {
            "At least one param must be not null - ean: $ean, material: $material"
        }

        viewModelScope.launch {
            navigator.showProgressLoadingData()

            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    ean = ean.orEmpty(),
                    material = material.orEmpty(),
                    taskType = task.value!!.taskType.code
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { goodInfo ->
                viewModelScope.launch {
                    if (manager.isGoodCanBeAdded(goodInfo)) {
                        isEanLastScanned = ean != null
                        isExistUnsavedData = true
                        addGood(goodInfo = goodInfo, number = ean ?: (material.orEmpty()))
                    } else {
                        if (manager.searchGoodFromList) {
                            manager.searchGoodFromList = false
                            manager.searchNumber = ""
                            navigator.goBack()
                        }

                        navigator.showGoodCannotBeAdded()
                    }
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        if (manager.searchGoodFromList) {
            manager.clearSearchFromListParams()
            navigator.goBack()
        }

        navigator.openAlertScreen(failure)
    }

    private fun addGood(goodInfo: GoodInfoResult, number: String) {
        viewModelScope.launch {
            goodInfo.apply {
                val commonUnits = database.getUnitsByCode(materialInfo.commonUnitsCode)
                val convertingUnits = database.getUnitsByCode(materialInfo.convertingUnitsCode)
                val innerQuantity = materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0
                val convertingInfo = if (commonUnits != convertingUnits) " (${commonUnits.name} = ${innerQuantity.dropZeros()} ${convertingUnits.name})" else ""

                good.value = GoodCreate(
                        ean = eanInfo.ean,
                        material = materialInfo.material,
                        name = materialInfo.name,
                        kind = getGoodKind(),
                        type = materialInfo.goodType,
                        control = getControlType(),
                        section = materialInfo.section,
                        matrix = getMatrixType(materialInfo.matrix),
                        commonUnits = commonUnits,
                        convertingUnits = convertingUnits,
                        innerQuantity = materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0,
                        convertingInfo = convertingInfo,
                        providers = providers.toMutableList(),
                        producers = producers.toMutableList()
                )
            }

            good.value?.let { good ->
                lastSuccessSearchNumber.value = number
                updateProviders(good.providers)
                updateProducers(good.producers)
                setScanModeFromGoodKind(good.kind)
                setDefaultQuantity(good)

                if (good.kind == GoodKind.EXCISE) {
                    navigator.showForExciseGoodNeedScanFirstMark()
                }
            }
        }
    }

    private fun loadMarkInfo(number: String) {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            markInfoNetRequest(MarkInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = good.value!!.material,
                    markNumber = number,
                    mode = 1,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                viewModelScope.launch {
                    result.status.let { status ->
                        if (status == MarkStatus.OK.code || status == MarkStatus.BAD.code) {
                            addMarkInfo(number, result)
                        } else if (status == MarkStatus.UNKNOWN.code) {
                            val alcoCode = BigInteger(number.substring(7, 19), 36).toString().padStart(19, '0')
                            database.getAlcoCodeInfoList(alcoCode).let { alcoCodeInfoList ->
                                if (alcoCodeInfoList.isNotEmpty()) {
                                    if (alcoCodeInfoList.find { it.material == good.value!!.material } != null) {
                                        addPartInfo(number, result)
                                    } else {
                                        navigator.openAlertScreen(resource.alcocodeDoesNotApplyToThisGood())
                                    }
                                } else {
                                    navigator.openAlertScreen(resource.unknownAlcocode())
                                }
                            }
                        } else {
                            navigator.openAlertScreen(result.statusDescription)
                        }
                    }
                }
            }
        }
    }

    private fun addMarkInfo(number: String, markInfo: MarkInfoResult) {
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        markInfoResult.value = markInfo
        quantityField.value = "1"

        when (number.length) {
            Constants.MARK_150 -> {
                scanModeType.value = ScanNumberType.MARK_150
                updateProducers(markInfo.producers.toMutableList())
                date.value = getFormattedDate(markInfo.producedDate, Constants.DATE_FORMAT_yyyy_mm_dd, Constants.DATE_FORMAT_dd_mm_yyyy)
            }
            Constants.MARK_68 -> {
                scanModeType.value = ScanNumberType.MARK_68
            }
        }
    }

    private fun addPartInfo(number: String, markInfo: MarkInfoResult) {
        scanModeType.value = ScanNumberType.PART
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        markInfoResult.value = markInfo
        quantityField.value = "1"
        updateProducers(markInfo.producers.toMutableList())
    }

    private fun loadBoxInfo(number: String) {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            markInfoNetRequest(MarkInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = good.value!!.material,
                    markNumber = number,
                    mode = 2,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                viewModelScope.launch {
                    result.status.let { status ->
                        if (status == BoxStatus.OK.code) {
                            addBoxInfo(number, result)
                        } else {
                            navigator.openAlertScreen(result.statusDescription)
                        }
                    }
                }
            }
        }
    }

    private fun addBoxInfo(number: String, markInfo: MarkInfoResult) {
        scanModeType.value = ScanNumberType.BOX
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        markInfoResult.value = markInfo
        quantityField.value = markInfo.marks.size.toString()
    }

    private suspend fun checkPart(): Either<Failure, MarkInfoResult> {
        navigator.showProgressLoadingData()

        return markInfoNetRequest(MarkInfoParams(
                tkNumber = sessionInfo.market.orEmpty(),
                material = good.value?.material.orEmpty(),
                producerCode = getProducerCode(),
                bottledDate = getFormattedDate(date.value.orEmpty(), Constants.DATE_FORMAT_dd_mm_yyyy, Constants.DATE_FORMAT_yyyy_mm_dd),
                mode = 3,
                quantity = quantity.value ?: 0.0
        )).also {
            navigator.hideProgress()
        }
    }

    private fun getProviderCode(): String {
        var providerCode = ""
        if (isProviderSelected.value == true) {
            providers.value?.let { providers ->
                providerPosition.value?.let { position ->
                    providerCode = providers.getOrNull(position)?.code.orEmpty()
                }
            }
        }

        return providerCode
    }

    private fun getProducerCode(): String {
        var producerCode = ""
        if (isProducerSelected.value == true) {
            producers.value?.let { producers ->
                producerPosition.value?.let { position ->
                    producerCode = producers[position].code
                }
            }
        }

        return producerCode
    }

    private fun getProvider(): ProviderInfo {
        var provider = ProviderInfo()
        if (isProviderSelected.value == true) {
            providers.value?.let { providers ->
                providerPosition.value?.let { position ->
                    provider = providers[position]
                }
            }
        }

        return provider
    }

    private fun getBasket(): Basket? {
        return task.value?.let { task ->
            good.value?.let { good ->
                task.baskets.find { basket ->
                    basket.section == good.section && basket.goodType == good.type && basket.control == good.control && basket.provider.code == getProviderCode()
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
        scanModeType.value?.let { type ->
            when (type) {
                ScanNumberType.COMMON -> addPosition()
                ScanNumberType.MARK_150, ScanNumberType.MARK_68 -> addMark()
                ScanNumberType.ALCOHOL, ScanNumberType.PART -> addPart()
                ScanNumberType.BOX -> addBox()
            }
        }
    }

    private fun addPosition() {
        good.value?.let { changedGood ->
            val position = Position(
                    quantity = quantity.value ?: 0.0,
                    provider = getProvider()
            )
            Logg.d { "--> add position = $position" }
            changedGood.addPosition(position)

            createBasket(changedGood)
            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addMark() {
        good.value?.let { changedGood ->
            val mark = Mark(
                    number = lastSuccessSearchNumber.value!!,
                    material = changedGood.material,
                    isBadMark = markInfoResult.value?.status == MarkStatus.BAD.code,
                    providerCode = getProviderCode(),
                    producerCode = getProducerCode()
            )
            Logg.d { "--> add mark = $mark" }
            changedGood.addMark(mark)

            createBasket(changedGood)
            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addPart() {
        good.value?.let { changedGood ->
            val part = Part(
                    number = lastSuccessSearchNumber.value!!,
                    material = changedGood.material,
                    quantity = quantity.value!!,
                    units = changedGood.convertingUnits,
                    providerCode = getProviderCode(),
                    producerCode = getProducerCode(),
                    date = date.value.orEmpty()
            )
            Logg.d { "--> add part = $part" }
            changedGood.addPart(part)

            createBasket(changedGood)
            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addBox() {
        good.value?.let { changedGood ->
            markInfoResult.value?.marks?.let { marks ->
                marks.forEach { mark ->
                    val markFromBox = Mark(
                            number = mark.number,
                            material = changedGood.material,
                            boxNumber = lastSuccessSearchNumber.value!!,
                            isBadMark = mark.isBadMark.isNotEmpty(),
                            providerCode = getProviderCode(),
                            producerCode = getProducerCode()
                    )
                    Logg.d { "--> add mark from box = $markFromBox" }
                    changedGood.addMark(markFromBox)
                }
            }

            createBasket(changedGood)
            manager.updateCurrentGood(changedGood)
        }
    }

    private fun createBasket(changedGood: GoodCreate) {
        if (getBasket() == null) {
            manager.addBasket(Basket(
                    section = changedGood.section,
                    goodType = changedGood.type,
                    control = changedGood.control,
                    provider = getProvider()
            ))
        }
    }

    fun updateData() {
        if (manager.isWasAddedProvider) {
            updateProviders(good.value!!.providers)
            providerPosition.value = 1
            manager.isWasAddedProvider = false
        }
    }

    /**
    Обработка нажатий кнопок
     */

    fun addProvider() {
        navigator.openAddProviderScreen()
    }

    fun onBackPressed() {
        if (isExistUnsavedData) {
            navigator.showUnsavedDataWillBeLost {
                manager.searchGoodFromList = false
                manager.searchNumber = ""
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
    }

    fun onClickRollback() {
        good.value?.let { good ->
            thereWasRollback = true
            updateProducers(good.producers)
            markInfoResult.value = null
            quantityField.value = "0"
            date.value = ""
        }
    }

    fun onClickDetails() {
        manager.updateCurrentGood(good.value)
        navigator.openGoodDetailsCreateScreen()
    }

    fun onClickApply() {
        when (scanModeType.value) {
            ScanNumberType.ALCOHOL, ScanNumberType.PART, ScanNumberType.MARK_68 -> {
                viewModelScope.launch {
                    checkPart().either(::handleFailure) { result ->
                        result.status.let { status ->
                            if (status == PartStatus.FOUND.code) {
                                saveChangesAndExit()
                            } else {
                                navigator.openAlertScreen(result.statusDescription)
                            }
                        }
                    }
                }
            }
            else -> saveChangesAndExit()
        }
    }

    private fun saveChangesAndExit() {
        saveChanges()
        manager.saveGoodInTask(good.value!!)
        isExistUnsavedData = false

        navigator.goBack()
        navigator.openBasketGoodListScreen()
    }

}