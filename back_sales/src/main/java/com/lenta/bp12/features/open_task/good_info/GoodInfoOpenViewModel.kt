package com.lenta.bp12.features.open_task.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.model.pojo.open_task.GoodOpen
import com.lenta.bp12.platform.extention.extractAlcoCode
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
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.*
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
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
    lateinit var scanInfoNetRequest: ScanInfoNetRequest

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

    private var originalSearchNumber = ""

    private var lastSuccessSearchNumber = ""

    val isCompactMode by lazy {
        good.map { good ->
            good?.kind == GoodKind.COMMON
        }
    }

    private val screenStatus = MutableLiveData(ScreenStatus.DEFAULT)

    val accountingType by lazy {
        screenStatus.map { status ->
            when (status) {
                ScreenStatus.MARK_150, ScreenStatus.MARK_68, ScreenStatus.TOBACCO, ScreenStatus.SHOES -> resource.typeMark()
                ScreenStatus.ALCOHOL, ScreenStatus.PART -> resource.typePart()
                else -> resource.typeQuantity()
            }
        }
    }

    val markScanEnabled by lazy {
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

    val quantity = quantityField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    val quantityFieldEnabled by lazy {
        screenStatus.map { status ->
            when (status) {
                ScreenStatus.COMMON, ScreenStatus.ALCOHOL, ScreenStatus.PART -> true
                else -> false
            }
        }
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
                val (good, entered) = it
                entered.sumWith(good.getTotalQuantity())
            }
        }
    }

    val totalWithUnits by lazy {
        totalQuantity.map { quantity ->
            "${quantity.dropZeros()} ${good.value?.commonUnits?.name}"
        }
    }

    /**
    Список производителей
     */

    private val sourceProducers = MutableLiveData(listOf<ProducerInfo>())

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

    val dateEnabled = screenStatus.map { status ->
        when (status) {
            ScreenStatus.ALCOHOL, ScreenStatus.PART -> true
            else -> false
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val applyEnabled by lazy {
        screenStatus.combineLatest(quantity).combineLatest(totalQuantity)
                .combineLatest(isProducerSelected).combineLatest(isCorrectDate).map {
                    it?.let {
                        val status = it.first.first.first.first
                        val enteredQuantity = it.first.first.first.second
                        val totalQuantity = it.first.first.second
                        val isProducerSelected = it.first.second
                        val isDateEntered = it.second

                        val isEnteredMoreThenZero = enteredQuantity > 0.0

                        when (status) {
                            ScreenStatus.COMMON -> enteredQuantity != 0.0 && totalQuantity > 0.0
                            ScreenStatus.ALCOHOL -> isEnteredMoreThenZero && isProducerSelected && isDateEntered
                            ScreenStatus.MARK_150 -> isEnteredMoreThenZero && isProducerSelected
                            ScreenStatus.MARK_68 -> isEnteredMoreThenZero && isProducerSelected
                            ScreenStatus.PART -> isEnteredMoreThenZero && isProducerSelected && isDateEntered
                            ScreenStatus.BOX -> isEnteredMoreThenZero && isProducerSelected
                            else -> false
                        }
                    } ?: false
                }
    }

    val detailsVisibility = screenStatus.map { status ->
        when (status) {
            ScreenStatus.MARK_150, ScreenStatus.MARK_68, ScreenStatus.PART -> true
            else -> false
        }
    }

    val rollbackVisibility = screenStatus.map { status ->
        when (status) {
            ScreenStatus.MARK_150, ScreenStatus.MARK_68, ScreenStatus.BOX -> true
            else -> false
        }
    }

    val rollbackEnabled = scanInfoResult.map { info ->
        info != null
    }

    val missingVisibility = screenStatus.map { status ->
        when (status) {
            ScreenStatus.COMMON, ScreenStatus.ALCOHOL, ScreenStatus.EXCISE -> true
            else -> false
        }
    }

    val missingEnabled by lazy {
        quantity.map {
            it ?: 0.0 == 0.0
        }
    }

    /**
    Блок инициализации
     */

    init {
        launchUITryCatch {
            manager.clearCurrentGood()
            checkSearchNumber(manager.searchNumber)
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


        actionByNumberLength(
                number = number,
                funcForEan = ::getGoodByEan,
                funcForMaterial = ::getGoodByMaterial,
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForExcise = ::loadExciseMarkInfo,
                funcForBox = ::loadBoxInfo,
                funcForNotValidFormat = {
                    goBackIfSearchFromList()
                    navigator.showIncorrectEanFormat()
                }
        )
    }

    private fun getGoodByEan(ean: String) {
        manager.findGoodByEan(ean)?.let { foundGood ->
            lastSuccessSearchNumber = ean
            isEanLastScanned = true
            setFoundGood(foundGood)
            return
        }

        if (task.value?.isStrict == false) {
            loadGoodInfoByEan(ean)
        } else {
            goBackIfSearchFromList()
            navigator.showGoodIsMissingInTask()
        }
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { foundGood ->
            lastSuccessSearchNumber = material
            isEanLastScanned = false
            setFoundGood(foundGood)
            return
        }

        if (task.value?.isStrict == false) {
            loadGoodInfoByMaterial(material)
        } else {
            goBackIfSearchFromList()
            navigator.showGoodIsMissingInTask()
        }
    }

    private fun setFoundGood(foundGood: GoodOpen) {
        manager.updateCurrentGood(foundGood)
        setScreenStatus(foundGood)
        setProducerList(foundGood)
        clearSpinnerPositions()
        setDefaultQuantity(foundGood)

        Logg.d { "--> found good: $foundGood" }
    }

    private fun setProducerList(good: GoodOpen) {
        if (good.kind == GoodKind.EXCISE) {
            updateProducers(emptyList())
        } else {
            updateProducers(good.producers)
        }
    }

    private fun setDefaultQuantity(good: GoodOpen) {
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

    private fun setScreenStatus(good: GoodOpen) {
        screenStatus.value = when (good.kind) {
            GoodKind.COMMON -> ScreenStatus.COMMON
            GoodKind.ALCOHOL -> ScreenStatus.ALCOHOL
            GoodKind.EXCISE -> ScreenStatus.EXCISE
            GoodKind.MARK -> when (good.markType) {
                MarkType.SHOES -> ScreenStatus.SHOES
                MarkType.TOBACCO -> ScreenStatus.TOBACCO
                MarkType.BEER -> ScreenStatus.BEER
                MarkType.CLOTHES -> ScreenStatus.CLOTHES
                MarkType.MEDICINE -> ScreenStatus.MEDICINE
                MarkType.MILK -> ScreenStatus.MILK
                MarkType.PERFUME -> ScreenStatus.PERFUME
                MarkType.PHOTO -> ScreenStatus.PHOTO
                MarkType.TIRES -> ScreenStatus.TIRES
                MarkType.UNKNOWN -> ScreenStatus.UNKNOWN
            }
        }
    }

    private fun clearSpinnerPositions() {
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
            if (manager.isGoodCorrespondToTask(result)) {
                if (manager.isGoodCanBeAdded(result)) {
                    isExistUnsavedData = true
                    setGood(result, number)
                } else {
                    goBackIfSearchFromList()
                    navigator.showGoodCannotBeAdded()
                }
            } else {
                goBackIfSearchFromList()
                navigator.showNotMatchTaskSettingsAddingNotPossible()
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
                good.value = GoodOpen(
                        ean = eanInfo?.ean.orEmpty(),
                        material = materialInfo?.material.orEmpty(),
                        name = materialInfo?.name.orEmpty(),
                        section = materialInfo?.section.orEmpty(),
                        matrix = getMatrixType(materialInfo?.matrix.orEmpty()),
                        kind = getGoodKind(),
                        commonUnits = database.getUnitsByCode(materialInfo?.commonUnitsCode.orEmpty()),
                        innerUnits = database.getUnitsByCode(materialInfo?.innerUnitsCode.orEmpty()),
                        innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull() ?: 0.0,
                        provider = task.value?.provider ?: ProviderInfo(),
                        producers = producers.orEmpty(),
                        markType = enumValueOrNull<MarkType>(materialInfo?.markType.orEmpty()).orIfNull { MarkType.UNKNOWN }
                )
            }

            good.value?.let { good ->
                lastSuccessSearchNumber = number
                setProducerList(good)
                clearSpinnerPositions()
                setScreenStatus(good)
                setDefaultQuantity(good)

                if (good.kind == GoodKind.EXCISE) {
                    navigator.showForExciseGoodNeedScanFirstMark()
                }

                Logg.d { "--> added good: $good" }
            }

            manager.clearSearchFromListParams()
        }
    }

    private fun loadExciseMarkInfo(number: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            scanInfoNetRequest(ScanInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = good.value?.material.orEmpty(),
                    markNumber = number,
                    mode = ScanInfoMode.MARK.mode,
                    quantity = 0.0
            )).also {
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
                    MarkStatus.OK.code -> addExciseMarkInfo(result)
                    MarkStatus.BAD.code -> {
                        addExciseMarkInfo(result)
                        navigator.openAlertScreen(result.statusDescription)
                    }
                    MarkStatus.UNKNOWN.code -> handleUnknownMark(number, result)
                    else -> navigator.openAlertScreen(result.statusDescription)
                }
            }
        }
    }

    private suspend fun handleUnknownMark(number: String, result: ScanInfoResult) {
        when (number.length) {
            Constants.MARK_150 -> navigator.openAlertScreen(result.statusDescription)
            Constants.MARK_68 -> {
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

    private fun addExciseMarkInfo(result: ScanInfoResult) {
        manager.clearSearchFromListParams()
        lastSuccessSearchNumber = originalSearchNumber
        isExistUnsavedData = true
        scanInfoResult.value = result
        quantityField.value = "1"

        when (originalSearchNumber.length) {
            Constants.MARK_150 -> {
                screenStatus.value = ScreenStatus.MARK_150
                updateProducers(result.producers.toMutableList())
                date.value = result.producedDate
            }
            Constants.MARK_68 -> {
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

        // todo Возможно ошибка в логике
        // потому что тогда непонятно зачем приходит новый список при запросе по марке
        //updateProducers(result.producers.toMutableList())
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
        screenStatus.value = ScreenStatus.BOX
        lastSuccessSearchNumber = originalSearchNumber
        isExistUnsavedData = true
        scanInfoResult.value = result
        quantityField.value = result.marks.size.toString()
        date.value = getFormattedDate(result.producedDate, Constants.DATE_FORMAT_yyyy_mm_dd, Constants.DATE_FORMAT_dd_mm_yyyy)
        updateProducers(result.producers.toMutableList())
    }

    private suspend fun checkPart(): Either<Failure, ScanInfoResult> {
        navigator.showProgressLoadingData(::handleFailure)

        return scanInfoNetRequest(ScanInfoParams(
                tkNumber = sessionInfo.market.orEmpty(),
                material = good.value?.material.orEmpty(),
                producerCode = getProducerCode(),
                bottledDate = date.value.orEmpty(),
                mode = ScanInfoMode.PART.mode,
                quantity = quantity.value ?: 0.0
        )).also {
            navigator.hideProgress()
        }
    }

    private fun handleCheckPartFailure(failure: Failure) {
        navigator.openAlertScreen(failure)
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

    private fun updateProducers(producers: List<ProducerInfo>) {
        sourceProducers.value = producers
    }

    private fun saveChanges() {
        screenStatus.value?.let { type ->
            when (type) {
                ScreenStatus.COMMON -> addPosition()
                ScreenStatus.MARK_150, ScreenStatus.MARK_68 -> addMark()
                ScreenStatus.ALCOHOL, ScreenStatus.PART -> addPart()
                ScreenStatus.BOX -> addBox()
                else -> Logg.e { "wrong screenStatus" }
            }

            good.value?.let { good ->
                manager.saveGoodInTask(good)
                isExistUnsavedData = false
            }
        }
    }

    private fun addPosition() {
        good.value?.let { changedGood ->
            changedGood.isCounted = true
            val position = Position(
                    quantity = quantity.value ?: 0.0,
                    provider = changedGood.provider
            )
            Logg.d { "--> add position = $position" }
            changedGood.addPosition(position)

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addMark() {
        good.value?.let { changedGood ->
            addEmptyPosition(changedGood)

            val mark = Mark(
                    number = lastSuccessSearchNumber,
                    isBadMark = scanInfoResult.value?.status == MarkStatus.BAD.code,
                    providerCode = changedGood.provider.code.orEmpty()
            )
            Logg.d { "--> add mark = $mark" }
            changedGood.addMark(mark)

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addPart() {
        good.value?.let { changedGood ->
            addEmptyPosition(changedGood)

            val part = Part(
                    number = lastSuccessSearchNumber,
                    material = changedGood.material,
                    quantity = quantity.value ?: 0.0,
                    providerCode = changedGood.provider.code.orEmpty(),
                    producerCode = getProducerCode(),
                    date = getDateFromString(date.value.orEmpty(), Constants.DATE_FORMAT_dd_mm_yyyy)
            )
            Logg.d { "--> add part = $part" }
            changedGood.addPart(part)

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addBox() {
        good.value?.let { changedGood ->
            addEmptyPosition(changedGood)

            scanInfoResult.value?.marks?.let { marks ->
                marks.forEach { mark ->
                    val markFromBox = Mark(
                            number = mark.number,
                            boxNumber = lastSuccessSearchNumber,
                            providerCode = changedGood.provider.code.orEmpty()
                    )
                    Logg.d { "--> add mark from box = $markFromBox" }
                    changedGood.addMark(markFromBox)
                }
            }

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addEmptyPosition(changedGood: GoodOpen) {
        changedGood.isCounted = true
        val position = Position(
                quantity = 0.0,
                provider = changedGood.provider
        )
        Logg.d { "--> add position = $position" }
        changedGood.addPosition(position)
    }

    /**
    Обработка нажатий кнопок
     */

    fun onBackPressed() {
        val enteredQuantity = quantity.value ?: 0.0
        if (isExistUnsavedData || enteredQuantity != 0.0) {
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
        good.value?.let {
            manager.updateCurrentGood(it)
            navigator.openGoodDetailsOpenScreen()
        }
    }

    fun onClickMissing() {
        good.value?.let { changedGood ->
            changedGood.isCounted = true
            manager.updateCurrentGood(changedGood)
            manager.updateCurrentTask(task.value)
        }

        navigator.goBack()
    }

    fun onClickApply() {
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

    private fun saveChangesAndExit() {
        saveChanges()
        navigator.goBack()
    }

}