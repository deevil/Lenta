package com.lenta.bp12.features.create_task.marked_good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.model.pojo.extentions.addPosition
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.ScanInfoParams
import com.lenta.bp12.request.ScanInfoResult
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.getFormattedDate
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class MarkedGoodInfoCreateViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var scanInfoNetRequest: ScanInfoNetRequest

    @Inject
    lateinit var database: IDatabaseRepository

    @Inject
    lateinit var resource: IResourceManager

    val selectedPage = MutableLiveData(0)

    /**
    Переменные
     */

    val task by unsafeLazy {
        manager.currentTask
    }

    val good by unsafeLazy {
        manager.currentGood
    }

    val title by unsafeLazy {
        good.map { good ->
            good?.getNameWithMaterial() ?: task.value?.getFormattedName()
        }
    }

    private var originalSearchNumber = ""

    private var lastSuccessSearchNumber = ""

    val accountingType by unsafeLazy {
        resource.typeMark()
    }

    private val scanInfoResult = MutableLiveData<ScanInfoResult>()

    private var isExistUnsavedData = false

    private var isEanLastScanned = false

    private var thereWasRollback = false

    val properties = MutableLiveData(listOf<GoodProperty>())

    val propertiesItems = MutableLiveData(listOf<GoodPropertyItem>())

    /**
    Ввод количества
     */

    val quantityField = MutableLiveData("0")

    val quantity = quantityField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    val quantityFieldEnabled by lazy {
        false
    }

    /**
    Количество товара итого
     */

    val totalTitle by lazy {
        good.map { good ->
            resource.totalWithConvertingInfo(good?.getConvertingInfo().orEmpty())
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

    private val basketQuantity by lazy {
        good.combineLatest(quantity).combineLatest(isProviderSelected).map {
            it?.let {
                val good = it.first.first
                val enteredQuantity = it.first.second
                val isProviderSelected = it.second

                if (isProviderSelected) {
                    getBasket()?.getQuantityOfGood(good)?.sumWith(enteredQuantity)
                            ?: enteredQuantity
                } else 0.0
            }
        }
    }

    val basketQuantityWithUnits by lazy {
        good.combineLatest(basketQuantity).map {
            it?.let {
                val (good, quantity) = it
                "${quantity.dropZeros()} ${good.commonUnits.name}"
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

    /**
    МРЦ
     */

    val mrc = MutableLiveData("")

    val isMrcVisible by unsafeLazy {
        good.map {
            it?.markType == MarkType.TOBACCO
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val applyEnabled by lazy {
        good.combineLatest(quantity)
                .combineLatest(totalQuantity)
                .combineLatest(basketQuantity)
                .combineLatest(isProviderSelected)
                .combineLatest(isProducerSelected)
                .combineLatest(isCorrectDate)
                .map {
                    it?.let {
                        val good = it.first.first.first.first.first.first
                        val enteredQuantity = it.first.first.first.first.first.second
                        val totalQuantity = it.first.first.first.first.second
                        val basketQuantity = it.first.first.first.second
                        val isProviderSelected = it.first.first.second
                        val isProducerSelected = it.first.second
                        val isDateEntered = it.second

                        val isEnteredMoreThenZero = enteredQuantity > 0.0
                        val isEnteredQuantityNotZero = enteredQuantity != 0.0
                        val isTotalQuantityMoreThenZero = totalQuantity > 0.0

                        when (good.markType) {
                            MarkType.TOBACCO -> isEnteredQuantityNotZero && isTotalQuantityMoreThenZero && basketQuantity > 0.0 && isProviderSelected
                            MarkType.SHOES -> isEnteredQuantityNotZero && isTotalQuantityMoreThenZero && basketQuantity > 0.0 && isProviderSelected
                            else -> false
                        }
                    } ?: false
                }
    }

    val rollbackVisibility = MutableLiveData(false)

    val rollbackEnabled = scanInfoResult.map { info ->
        info != null
    }

    /**
    Блок инициализации
     */

    init {
        launchUITryCatch {
            good.value?.let{
                Logg.e{ it.toString() }
            }.orIfNull {
                manager.clearCurrentGood()
                checkSearchNumber(manager.searchNumber)
            }
        }
    }

    /**
    Методы
     */

    fun onScanResult(number: String) {
        good.value?.let { good ->
            if (applyEnabled.value == true || (good.kind == GoodKind.EXCISE && isExciseNumber(number))) {
                if (!thereWasRollback) {
                    saveChanges()
                } else {
                    thereWasRollback = false
                }

                manager.clearSearchFromListParams()
                checkSearchNumber(number)
            }
        }
    }

    private fun isExciseNumber(number: String): Boolean {
        return when (number.length) {
            Constants.MARK_150, Constants.MARK_68, Constants.BOX_26 -> true
            else -> false
        }
    }

    private fun checkSearchNumber(number: String) {
        originalSearchNumber = number
        good.value?.let { goodValue ->
            Logg.e {
                goodValue.toString()
            }
            actionByNumber(
                    number = number,
                    funcForEan = ::getGoodByEan,
                    funcForMaterial = ::getGoodByMaterial,
                    funcForSapOrBar = navigator::showTwelveCharactersEntered,
                    funcForBox = ::loadBoxInfo,
                    funcForNotValidBarFormat = {
                        goBackIfSearchFromList()
                        navigator.showIncorrectEanFormat()
                    }
            )
        }
    }

    private fun getGoodByEan(ean: String) {
        manager.findGoodByEan(ean)?.let { foundGood ->
            lastSuccessSearchNumber = ean
            isEanLastScanned = true
            setFoundGood(foundGood)
        } ?: loadGoodInfoByEan(ean)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { foundGood ->
            lastSuccessSearchNumber = material
            isEanLastScanned = false
            setFoundGood(foundGood)
        } ?: loadGoodInfoByMaterial(material)
    }

    private fun setFoundGood(foundGood: GoodCreate) {
        manager.updateCurrentGood(foundGood)
        updateProviders(foundGood.providers)
        updateProducers(foundGood.producers)
        clearSpinnerPositions()
        setDefaultQuantity(foundGood)

        Logg.d { "--> found good: $foundGood" }
    }

    private fun setDefaultQuantity(good: GoodCreate) {
        quantityField.value = if (good.kind == GoodKind.COMMON) {
            if (good.isDifferentUnits()) {
                with(ScanCodeInfo(originalSearchNumber)) {
                    val converted = if (weight > 0.0) getConvertedQuantity(good.innerQuantity) else 0.0
                    converted.dropZeros()
                }
            } else {
                if (isEanLastScanned) "1" else "0"
            }
        } else "0"
    }

    private fun clearSpinnerPositions() {
        providerPosition.value = 0
        producerPosition.value = 0
    }

    private fun loadGoodInfoByEan(ean: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    ean = ean,
                    taskType = task.value?.type?.code.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                isEanLastScanned = true
                handleLoadGoodInfoResult(result, ean)
            }
        }
    }

    private fun loadGoodInfoByMaterial(material: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = material,
                    taskType = task.value?.type?.code.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                isEanLastScanned = false
                handleLoadGoodInfoResult(result, material)
            }
        }
    }

    private fun handleLoadGoodInfoResult(result: GoodInfoResult, number: String) {
        launchUITryCatch {
            if (manager.isGoodCanBeAdded(result)) {
                isExistUnsavedData = true
                setGood(result, number)
            } else {
                goBackIfSearchFromList()
                navigator.showGoodCannotBeAdded()
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        goBackIfSearchFromList()
        navigator.openAlertScreen(failure)
    }

    private fun goBackIfSearchFromList() {
        if (manager.isSearchFromList) {
            manager.clearSearchFromListParams()
            navigator.goBack()
        }
    }

    private fun setGood(result: GoodInfoResult, number: String) {
        launchUITryCatch {
            with(result) {
                materialInfo?.let {
                    good.value = GoodCreate(
                            ean = eanInfo?.ean.orEmpty(),
                            eans = database.getEanListByMaterialUnits(
                                    material = materialInfo.material.orEmpty(),
                                    unitsCode = materialInfo.commonUnitsCode.orEmpty()
                            ),
                            material = materialInfo.material.orEmpty(),
                            name = materialInfo.name.orEmpty(),
                            kind = getGoodKind(),
                            type = materialInfo.goodType.orEmpty(),
                            control = getControlType(),
                            section = materialInfo.section.orEmpty(),
                            matrix = getMatrixType(materialInfo.matrix.orEmpty()),
                            commonUnits = database.getUnitsByCode(materialInfo.commonUnitsCode.orEmpty()),
                            innerUnits = database.getUnitsByCode(materialInfo.innerUnitsCode.orEmpty()),
                            innerQuantity = materialInfo.innerQuantity?.toDoubleOrNull() ?: 1.0,
                            providers = providers.orEmpty().toMutableList(),
                            producers = producers.orEmpty().toMutableList(),
                            markType = getMarkType(),
                            maxRetailPrice = "",
                            volume = materialInfo.volume?.toDoubleOrNull() ?: 0.0
                    )
                }.orIfNull {
                    Logg.e { "materialInfo null" }
                    navigator.showInternalError(resource.goodNotFoundErrorMsg)
                }
            }

            good.value?.let { good ->
                lastSuccessSearchNumber = number
                updateProviders(good.providers)
                updateProducers(good.producers)
                clearSpinnerPositions()
                setDefaultQuantity(good)

                if (good.kind == GoodKind.EXCISE) {
                    navigator.showForExciseGoodNeedScanFirstMark()
                }

                Logg.d { "--> added good: $good" }
            }

            manager.clearSearchFromListParams()
        }
    }

    private fun loadBoxInfo(number: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            scanInfoNetRequest(ScanInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = good.value?.material.orEmpty(),
                    boxNumber = number,
                    mode = ScanInfoMode.BOX.mode,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure, ::handleLoadBoxInfoResult)
        }
    }

    private fun handleLoadBoxInfoResult(result: ScanInfoResult) {
        launchUITryCatch {
            when (result.status) {
                BoxStatus.OK.code -> addBoxInfo(result)
                else -> navigator.openAlertScreen(result.statusDescription)
            }
        }
    }

    private fun addBoxInfo(result: ScanInfoResult) {
        lastSuccessSearchNumber = originalSearchNumber
        isExistUnsavedData = true
        scanInfoResult.value = result
        quantityField.value = result.exciseMarks.size.toString()
        date.value = getFormattedDate(result.producedDate, Constants.DATE_FORMAT_yyyy_mm_dd, Constants.DATE_FORMAT_dd_mm_yyyy)
        updateProducers(result.producers.toMutableList())
    }

    private suspend fun checkPart(): Either<Failure, ScanInfoResult> {
        navigator.showProgressLoadingData(::handleFailure)

        return scanInfoNetRequest(ScanInfoParams(
                tkNumber = sessionInfo.market.orEmpty(),
                material = good.value?.material.orEmpty(),
                producerCode = getProducerCode(),
                bottledDate = getFormattedDate(date.value.orEmpty(), Constants.DATE_FORMAT_dd_mm_yyyy, Constants.DATE_FORMAT_yyyy_mm_dd),
                mode = ScanInfoMode.PART.mode,
                quantity = quantity.value ?: 0.0
        )).also {
            navigator.hideProgress()
        }
    }

    private fun handleCheckPartFailure(failure: Failure) {
        navigator.openAlertScreen(failure)
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
                    producerCode = producers.getOrNull(position)?.code.orEmpty()
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
                    basket.section == good.section &&
                            basket.goodType == good.type &&
                            basket.control == good.control &&
                            basket.provider?.code == getProviderCode()
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
        good.value?.let { good ->
            manager.saveGoodInTask(good)
            isExistUnsavedData = false
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private suspend fun addPosition() {
        good.value?.let { changedGood ->
            val quantityValue = quantity.value ?: 0.0
            val position = Position(
                    quantity = quantityValue,
                    provider = getProvider()
            )
            position.materialNumber = changedGood.material
            changedGood.addPosition(position)
            manager.addGoodToBasket(
                    good = changedGood,
                    provider = getProvider(),
                    count = quantityValue)
            manager.updateCurrentGood(changedGood)
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private suspend fun addMark() {
        good.value?.let { changedGood ->
            val mark = Mark(
                    number = lastSuccessSearchNumber,
                    isBadMark = scanInfoResult.value?.status == ExciseMarkStatus.BAD.code,
                    providerCode = getProviderCode()
            )
            manager.addGoodToBasketWithMark(changedGood, mark, getProvider())
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private suspend fun addBox() {
        good.value?.let { changedGood ->

            scanInfoResult.value?.exciseMarks?.let { marks ->
                marks.forEach { mark ->
                    val markFromBox = Mark(
                            number = mark.number.orEmpty(),
                            boxNumber = lastSuccessSearchNumber,
                            providerCode = getProviderCode()
                    )
                    Logg.d { "--> add mark from box = $markFromBox" }
                    manager.addGoodToBasketWithMark(changedGood, markFromBox, getProvider())
                }
            }

            manager.updateCurrentGood(changedGood)
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private fun addEmptyPosition(changedGood: GoodCreate) {
        val position = Position(
                quantity = 0.0,
                provider = getProvider()
        )
        Logg.d { "--> add position = $position" }
        changedGood.addPosition(position)
    }

    fun updateData() {
        val good = good.value
        if (manager.isWasAddedProvider && good != null) {
            updateProviders(good.providers)
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
                manager.clearSearchFromListParams()
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
            scanInfoResult.value = null
            quantityField.value = "0"
            date.value = ""
        }
    }

    fun onClickDetails() {
        manager.updateCurrentGood(good.value)
        navigator.openGoodDetailsCreateScreen()
    }

    fun onClickApply() {

        launchUITryCatch {
            checkPart().either(::handleCheckPartFailure) { result ->
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

    private fun saveChangesAndExit() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            saveChanges()
            navigator.hideProgress()
            navigator.goBack()
            navigator.openBasketCreateGoodListScreen()
            manager.isBasketsNeedsToBeClosed = false
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int){

    }

}