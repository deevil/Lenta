package com.lenta.bp12.features.create_task.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.create_task.base_good_info.BaseGoodInfoCreateViewModel
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.model.pojo.extentions.addMark
import com.lenta.bp12.model.pojo.extentions.addMarks
import com.lenta.bp12.model.pojo.extentions.addPosition
import com.lenta.bp12.platform.DEFAULT_POSITION
import com.lenta.bp12.platform.DEFAULT_QUANTITY
import com.lenta.bp12.platform.ZERO_VOLUME
import com.lenta.bp12.platform.extention.extractAlcoCode
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.ScanInfoParams
import com.lenta.bp12.request.ScanInfoResult
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.getDateFromString
import com.lenta.shared.utilities.getFormattedDate
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class GoodInfoCreateViewModel : BaseGoodInfoCreateViewModel() {

    @Inject
    override lateinit var navigator: IScreenNavigator

    @Inject
    override lateinit var manager: ICreateTaskManager

    @Inject
    override lateinit var sessionInfo: ISessionInfo

    /** Получение данных товара по ШК\SAP-коду
     * "ZMP_UTZ_BKS_05_V001" */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    /** Получение данных по акцизному товару
     * "ZMP_UTZ_100_V001" */
    @Inject
    override lateinit var scanInfoNetRequest: ScanInfoNetRequest

    @Inject
    override lateinit var database: IDatabaseRepository

    @Inject
    override lateinit var resource: IResourceManager

    /**
    Переменные
     */

    private var originalSearchNumber = ""

    private var lastSuccessSearchNumber = ""

    val isCommonGood by lazy {
        good.map { good ->
            good?.kind == GoodKind.COMMON
        }
    }

    private val screenStatus = MutableLiveData(ScreenStatus.DEFAULT)

    val accountingType by unsafeLazy {
        screenStatus.map { status ->
            when (status) {
                ScreenStatus.MARK_150, ScreenStatus.MARK_68 -> resource.typeMark()
                ScreenStatus.ALCOHOL, ScreenStatus.PART -> resource.typePart()
                else -> resource.typeQuantity()
            }
        }
    }

    val markScanEnabled by unsafeLazy {
        good.map { good ->
            good?.kind == GoodKind.EXCISE
        }
    }

    private val scanInfoResult = MutableLiveData<ScanInfoResult>()

    private var isExistUnsavedData = false

    private var isEanLastScanned = false

    private var thereWasRollback = false

    /**
    Ввод количества
     */

    val quantityField = MutableLiveData("0")

    override val quantity = quantityField.map {
        it?.toDoubleOrNull() ?: DEFAULT_QUANTITY
    }

    override val quantityFieldEnabled by unsafeLazy {
        screenStatus.map { status ->
            when (status) {
                ScreenStatus.COMMON, ScreenStatus.ALCOHOL, ScreenStatus.PART -> true
                else -> false
            }
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

    val producerPosition = MutableLiveData(DEFAULT_POSITION)

    private val isProducerSelected = producerEnabled.combineLatest(producerPosition).map {
        val isEnabled = it?.first ?: false
        val position = it?.second ?: DEFAULT_POSITION

        (isEnabled && position > DEFAULT_POSITION) || (!isEnabled && position == DEFAULT_POSITION)
    }

    /**
    Дата производства
     */

    val date = MutableLiveData("")

    private val isCorrectDate = date.map { date ->
        (date?.length ?: 0) == DATE_STRING_LENGTH
    }

    val dateEnabled = screenStatus.map { status ->
        (status == ScreenStatus.ALCOHOL || status == ScreenStatus.PART)
    }

    /**
    Кнопки нижнего тулбара
     */
    override val applyEnabled by lazy {
        screenStatus.switchMap { status ->
            quantity.switchMap { enteredQuantity ->
                totalQuantity.switchMap { totalQuantity ->
                    basketQuantity.switchMap { basketQuantity ->
                        isProviderSelected.switchMap { isProviderSelected ->
                            isProducerSelected.switchMap { isProducerSelected ->
                                isCorrectDate.switchMap { isDateEntered ->
                                    liveData {
                                        val isEnteredMoreThanZero = enteredQuantity > DEFAULT_QUANTITY
                                        val isEnteredMoreThanZeroAndProviderSelected = isEnteredMoreThanZero && isProviderSelected

                                        val result = when (status) {
                                            ScreenStatus.COMMON ->
                                                enteredQuantity != DEFAULT_QUANTITY &&
                                                        totalQuantity > DEFAULT_QUANTITY &&
                                                        basketQuantity > DEFAULT_QUANTITY &&
                                                        isProviderSelected
                                            ScreenStatus.ALCOHOL -> isEnteredMoreThanZeroAndProviderSelected && isProducerSelected && isDateEntered
                                            ScreenStatus.MARK_150 -> isEnteredMoreThanZeroAndProviderSelected
                                            ScreenStatus.MARK_68 -> isEnteredMoreThanZeroAndProviderSelected && isProducerSelected
                                            ScreenStatus.PART -> isEnteredMoreThanZeroAndProviderSelected && isProducerSelected && isDateEntered
                                            ScreenStatus.BOX -> isEnteredMoreThanZeroAndProviderSelected && isProducerSelected
                                            else -> false
                                        }
                                        emit(result)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override val rollbackVisibility = screenStatus.map { status ->
        isStatusForRollback(status)
    }

    val rollbackEnabled = scanInfoResult.map { info ->
        info != null
    }

    /**
    Блок инициализации
     */

    init {
        onInitGoodInfo()
    }

    /**
    Методы
     */
    override fun onScanResult(number: String) {
        good.value?.let {
            launchUITryCatch {
                if (isApplyEnabled()) {
                    savePreviousScannedExcise()
                }
                checkSearchNumber(number)
            }
        }.orIfNull {
            Logg.e { "good is null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private suspend fun savePreviousScannedExcise() {
        if (!thereWasRollback) {
            when (screenStatus.value) {
                ScreenStatus.ALCOHOL, ScreenStatus.PART -> {
                    checkPart().either(
                            fnL = ::handleCheckPartFailure,
                            fnR = ::handleCheckPartSuccessResult
                    )
                }
                else -> saveChanges()
            }
        } else {
            thereWasRollback = false
        }
    }

    private fun isStatusForRollback(status: ScreenStatus?): Boolean {
        return (status == ScreenStatus.MARK_150 || status == ScreenStatus.MARK_68 || status == ScreenStatus.BOX)
    }

    private fun handleCheckPartSuccessResult(result: ScanInfoResult) {
        launchUITryCatch {
            result.status.let { status ->
                if (status == PartStatus.FOUND.code) {
                    saveChanges()
                } else {
                    navigator.openAlertScreen(result.statusDescription)
                }
            }
        }
    }

    private fun isApplyEnabled() = applyEnabled.value == true

    override fun checkSearchNumber(number: String) {
        originalSearchNumber = number
        actionByNumber(
                number = number,
                funcForEan = ::getGoodByEan,
                funcForMaterial = ::getGoodByMaterial,
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForExcise = ::loadExciseMarkInfo,
                funcForBox = ::loadBoxInfo,
                funcForNotValidBarFormat = {
                    navigator.showIncorrectEanFormat()
                }
        )
    }

    /**
     * Метод ищет есть ли уже товар в задании по EAN,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByEan(ean: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByEan(ean) }
            navigator.hideProgress()
            foundGood?.let {
                lastSuccessSearchNumber = ean
                isEanLastScanned = true
                setFoundGood(it)
            }.orIfNull { loadGoodInfoByEan(ean) }
        }
    }

    /**
     * Метод ищет есть ли уже товар в задании по Sap коду,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByMaterial(material: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByMaterial(material) }
            navigator.hideProgress()
            foundGood?.let {
                lastSuccessSearchNumber = material
                isEanLastScanned = false
                setFoundGood(it)
            }.orIfNull { loadGoodInfoByMaterial(material) }
        }
    }

    private fun setFoundGood(foundGood: Good) {
        manager.updateCurrentGood(foundGood)

        if (foundGood.isExciseAlco()) {
            navigator.showForExciseGoodNeedScanFirstMark()
        }

        setScreenStatus(foundGood)
        updateProviders(foundGood.providers)
        updateProducers(foundGood.producers)
        clearSpinnerPositions()
        setDefaultQuantity(foundGood)

        Logg.d { "--> found good: $foundGood" }
    }

    private fun setDefaultQuantity(good: Good) {
        quantityField.value = if (good.kind == GoodKind.COMMON) {
            if (good.isDifferentUnits()) {
                with(ScanCodeInfo(originalSearchNumber)) {
                    val converted =
                            if (weight > 0.0) getConvertedQuantity(good.innerQuantity)
                            else DEFAULT_QUANTITY
                    converted.dropZeros()
                }
            } else {
                if (isEanLastScanned) "1"
                else DEFAULT_QUANTITY_FIELD
            }
        } else DEFAULT_QUANTITY_FIELD
    }

    private fun setScreenStatus(good: Good) {
        screenStatus.value = when (good.kind) {
            GoodKind.COMMON -> ScreenStatus.COMMON
            GoodKind.ALCOHOL -> ScreenStatus.ALCOHOL
            GoodKind.EXCISE -> ScreenStatus.EXCISE
            GoodKind.MARK -> ScreenStatus.MARK
            else -> ScreenStatus.VET
        }
    }

    private fun clearSpinnerPositions() {
        providerPosition.value = DEFAULT_POSITION
        producerPosition.value = DEFAULT_POSITION
    }

    private suspend fun loadGoodInfoByEan(ean: String) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(
                GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        ean = ean,
                        taskType = task.value?.type?.code.orEmpty()
                )
        ).also {
            navigator.hideProgress()
        }.either(::handleFailure) { result ->
            isEanLastScanned = true
            handleLoadGoodInfoResult(result, ean)
        }
    }

    private suspend fun loadGoodInfoByMaterial(material: String) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(
                GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        material = material,
                        taskType = task.value?.type?.code.orEmpty()
                )
        ).also {
            navigator.hideProgress()
        }.either(::handleFailure) { result ->
            isEanLastScanned = false
            handleLoadGoodInfoResult(result, material)
        }
    }

    private fun handleLoadGoodInfoResult(result: GoodInfoResult, number: String) {
        launchUITryCatch {
            val isGoodCanBeAdded = manager.isGoodCanBeAdded(result)
            val isWholesaleTask = manager.isWholesaleTaskType
            val goodKind = result.getGoodKind()
            val isGoodVet = goodKind == GoodKind.VET
            val isGoodExcise = goodKind == GoodKind.EXCISE
            with(navigator) {
                when {
                    isWholesaleTask && isGoodVet -> showCantAddVetToWholeSale()
                    isWholesaleTask && isGoodExcise -> showCantAddExciseGoodForWholesale()
                    isGoodCanBeAdded -> {
                        isExistUnsavedData = true
                        setGood(result, number)
                    }
                    else -> showGoodCannotBeAdded()
                }
            }
        }
    }

    private fun setGood(result: GoodInfoResult, number: String) {
        launchUITryCatch {
            with(result) {
                val good = Good(
                        ean = eanInfo?.ean.orEmpty(),
                        eans = database.getEanListByMaterialUnits(
                                material = materialInfo?.material.orEmpty(),
                                unitsCode = materialInfo?.commonUnitsCode.orEmpty()
                        ),
                        material = materialInfo?.material.orEmpty(),
                        name = materialInfo?.name.orEmpty(),
                        kind = getGoodKind(),
                        type = materialInfo?.goodType.orEmpty(),
                        control = getControlType(),
                        section = materialInfo?.section.orEmpty(),
                        matrix = getMatrixType(materialInfo?.matrix.orEmpty()),
                        commonUnits = database.getUnitsByCode(materialInfo?.commonUnitsCode.orEmpty()),
                        innerUnits = database.getUnitsByCode(materialInfo?.innerUnitsCode.orEmpty()),
                        innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull() ?: 1.0,
                        providers = providers.orEmpty().toMutableList(),
                        producers = producers.orEmpty().toMutableList(),
                        volume = materialInfo?.volume?.toDoubleOrNull() ?: ZERO_VOLUME,
                        purchaseGroup = materialInfo?.purchaseGroup.orEmpty()
                )

                lastSuccessSearchNumber = number
                setFoundGood(good)
            }
        }
    }

    private fun loadExciseMarkInfo(number: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            scanInfoNetRequest(
                    ScanInfoParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            material = good.value?.material.orEmpty(),
                            markNumber = number,
                            mode = ScanInfoMode.MARK.mode,
                            quantity = DEFAULT_QUANTITY
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleLoadExciseMarkInfoResult(result, number)
            }
        }
    }

    private fun handleLoadExciseMarkInfoResult(result: ScanInfoResult, number: String) {
        launchUITryCatch {
            result.status.let { status ->
                when (status) {
                    ExciseMarkStatus.OK.code -> addMarkExciseInfo(result)
                    ExciseMarkStatus.BAD.code -> {
                        addMarkExciseInfo(result)
                        navigator.openAlertScreen(result.statusDescription)
                    }
                    ExciseMarkStatus.UNKNOWN.code -> handleUnknownMark(number, result)
                    else -> navigator.openAlertScreen(result.statusDescription)
                }
            }
        }
    }

    private suspend fun handleUnknownMark(number: String, result: ScanInfoResult) {
        when (number.length) {
            Constants.EXCISE_MARK_150 -> navigator.openAlertScreen(result.statusDescription)
            Constants.EXCISE_MARK_68 -> {
                val alcoCodeInfoList = database.getAlcoCodeInfoList(number.extractAlcoCode())

                if (alcoCodeInfoList.isEmpty()) {
                    navigator.openAlertScreen(resource.unknownAlcocode())
                    return
                }

                if (alcoCodeInfoList.find { it.material == good.value?.material } != null) {
                    addPartInfo(result)
                } else {
                    navigator.openAlertScreen(resource.alcocodeDoesNotApplyToThisGood())
                }
            }
        }
    }

    private fun addMarkExciseInfo(result: ScanInfoResult) {
        lastSuccessSearchNumber = originalSearchNumber
        isExistUnsavedData = true
        scanInfoResult.value = result
        quantityField.value = "1"

        when (originalSearchNumber.length) {
            Constants.EXCISE_MARK_150 -> {
                screenStatus.value = ScreenStatus.MARK_150
                updateProducers(result.producers.toMutableList())
                date.value = getFormattedDate(result.producedDate, Constants.DATE_FORMAT_yyyy_mm_dd, Constants.DATE_FORMAT_dd_mm_yyyy)
            }
            Constants.EXCISE_MARK_68 -> {
                screenStatus.value = ScreenStatus.MARK_68
            }
        }
    }

    private fun addPartInfo(result: ScanInfoResult) {
        screenStatus.value = ScreenStatus.PART
        lastSuccessSearchNumber = originalSearchNumber
        isExistUnsavedData = true
        scanInfoResult.value = result
        quantityField.value = "1"
    }

    override fun loadBoxInfo(number: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            scanInfoNetRequest(
                    ScanInfoParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            material = good.value?.material.orEmpty(),
                            boxNumber = number,
                            mode = ScanInfoMode.BOX.mode,
                            quantity = DEFAULT_QUANTITY
                    )
            ).also {
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
        screenStatus.value = ScreenStatus.BOX
        lastSuccessSearchNumber = originalSearchNumber
        isExistUnsavedData = true
        scanInfoResult.value = result
        quantityField.value = result.exciseMarks.size.toString()
        date.value = getFormattedDate(result.producedDate, Constants.DATE_FORMAT_yyyy_mm_dd, Constants.DATE_FORMAT_dd_mm_yyyy)
        updateProducers(result.producers.toMutableList())
    }

    private suspend fun checkPart(): Either<Failure, ScanInfoResult> {
        navigator.showProgressLoadingData(::handleFailure)

        val formattedDate = getFormattedDate(
                date = date.value.orEmpty(),
                sourcePattern = Constants.DATE_FORMAT_dd_mm_yyyy,
                targetPattern = Constants.DATE_FORMAT_yyyy_mm_dd
        )

        val quantityFromField = quantity.value ?: DEFAULT_QUANTITY

        val allPartsQuantity = good.value?.getPartQuantityByDateAndProducer(
                date = date.value.orEmpty(),
                producerCode = getProducerCode(),
                quantityFromField = quantityFromField
        ) ?: quantityFromField

        return scanInfoNetRequest(
                ScanInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        material = good.value?.material.orEmpty(),
                        producerCode = getProducerCode(),
                        bottledDate = formattedDate,
                        mode = ScanInfoMode.PART.mode,
                        quantity = allPartsQuantity
                )
        ).also {
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

    private fun updateProducers(producers: MutableList<ProducerInfo>) {
        sourceProducers.value = producers
    }

    override suspend fun saveChanges() {
        screenStatus.value?.let { status ->
            good.value?.let { good ->
                manager.saveGoodInTask(good)
                isExistUnsavedData = false
            }.orIfNull {
                Logg.e { "good null" }
                navigator.showInternalError(resource.goodNotFoundErrorMsg)
            }

            when (status) {
                ScreenStatus.COMMON -> addPosition()
                ScreenStatus.MARK_150, ScreenStatus.MARK_68 -> addMark()
                ScreenStatus.ALCOHOL, ScreenStatus.PART -> addPart()
                ScreenStatus.BOX -> addBox()
                else -> Unit
            }
        }
    }

    private suspend fun addPosition() {
        good.value?.let { changedGood ->
            val quantityValue = quantity.value ?: DEFAULT_QUANTITY
            val position = Position(
                    quantity = quantityValue,
                    provider = getProvider()
            )
            position.materialNumber = changedGood.material
            changedGood.addPosition(position)
            manager.addGoodToBasket(
                    good = changedGood,
                    provider = getProvider(),
                    count = quantityValue
            )
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
            changedGood.addMark(mark)
            manager.addGoodToBasketWithMark(
                    good = changedGood,
                    mark = mark,
                    provider = getProvider()
            )
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private suspend fun addPart() {
        good.value?.let { changedGood ->
            val quantityValue = quantity.value ?: DEFAULT_QUANTITY

            val localDate = date.value?.let {
                try {
                    getDateFromString(it, Constants.DATE_FORMAT_dd_mm_yyyy)
                } catch (e: RuntimeException) {
                    Date()
                }
            } ?: Date()

            val part = Part(
                    number = lastSuccessSearchNumber,
                    material = changedGood.material,
                    providerCode = getProviderCode(),
                    producerCode = getProducerCode(),
                    date = localDate
            )
            manager.addGoodToBasket(
                    good = changedGood,
                    part = part,
                    provider = getProvider(),
                    count = quantityValue
            )
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private suspend fun addBox() {
        good.value?.let { changedGood ->
            scanInfoResult.value?.exciseMarks?.let { marks ->

                val mappedMarks = marks.map { mark ->
                    Mark(
                            number = mark.number.orEmpty(),
                            boxNumber = lastSuccessSearchNumber,
                            providerCode = getProviderCode()
                    )
                }
                changedGood.addMarks(mappedMarks)
                mappedMarks.forEach { markFromBox ->
                    Logg.d { "--> add mark from box = $markFromBox" }
                    manager.addGoodToBasketWithMark(
                            good = changedGood,
                            mark = markFromBox,
                            provider = getProvider()
                    )
                }
            }
            manager.updateCurrentGood(changedGood)
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    /**
    Обработка нажатий кнопок
     */


    override fun onBackPressed() {
        if (isExistUnsavedData) {
            navigator.showUnsavedDataWillBeLost {
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
    }

    override fun onClickRollback() {
        good.value?.let { good ->
            thereWasRollback = true
            updateProducers(good.producers)
            scanInfoResult.value = null
            quantityField.value = DEFAULT_QUANTITY_FIELD
            date.value = ""
        }
    }

    override fun onClickApply() {
        when (screenStatus.value) {
            ScreenStatus.ALCOHOL, ScreenStatus.PART -> {
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
            else -> saveChangesAndExit()
        }
    }

    override fun saveChangesAndExit() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            saveChanges()
            navigator.hideProgress()
            navigator.openBasketCreateGoodListScreen()
            manager.isBasketsNeedsToBeClosed = false
        }
    }

    private fun onInitGoodInfo() {
        launchUITryCatch {
            good.value?.let {
                setFoundGood(it)
            }.orIfNull {
                Logg.e { "good null" }
                navigator.showInternalError(resource.goodNotFoundErrorMsg)
            }
        }
    }

    companion object {
        private const val DEFAULT_QUANTITY_FIELD = "0"
        private const val DATE_STRING_LENGTH = 10
    }
}