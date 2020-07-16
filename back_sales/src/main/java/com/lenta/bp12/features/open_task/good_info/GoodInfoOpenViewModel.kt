package com.lenta.bp12.features.open_task.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.getDateFromString
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

    private val lastSuccessSearchNumber = MutableLiveData("")

    val isCompactMode by lazy {
        good.map { good ->
            good?.kind == GoodKind.COMMON
        }
    }

    private val screenStatus = MutableLiveData(ScreenStatus.DEFAULT)

    val accountingType by lazy {
        screenStatus.map { status ->
            when (status) {
                ScreenStatus.MARK_150, ScreenStatus.MARK_68 -> resource.typeMark()
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
            resource.totalWithConvertingInfo(good?.convertingInfo.orEmpty())
        }
    }

    private val totalQuantity by lazy {
        good.combineLatest(quantity).map {
            it?.let {
                val (good, entered) = it
                if (good.isCounted) good.getQuantity() else entered.sumWith(good.getTotalQuantity())
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
        screenStatus.combineLatest(quantity)
                .combineLatest(isProducerSelected)
                .combineLatest(isCorrectDate).map {
                    it?.let {
                        val status = it.first.first.first
                        val isEnteredQuantity = it.first.first.second > 0.0
                        val isProducerSelected = it.first.second
                        val isDateEntered = it.second

                        when (status) {
                            ScreenStatus.COMMON -> isEnteredQuantity
                            ScreenStatus.ALCOHOL -> isEnteredQuantity && isProducerSelected && isDateEntered
                            ScreenStatus.MARK_150 -> isEnteredQuantity && isProducerSelected
                            ScreenStatus.MARK_68 -> isEnteredQuantity && isProducerSelected
                            ScreenStatus.PART -> isEnteredQuantity && isProducerSelected && isDateEntered
                            ScreenStatus.BOX -> isEnteredQuantity && isProducerSelected
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
        if (task.value?.isStrict == true && (number.length < Constants.SAP_6 || number.length > Constants.MARK_150)) {
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
            if (foundGood.isDataLoaded || foundGood.isCounted) {
                lastSuccessSearchNumber.value = eanWithoutWeight
                setFoundGood(foundGood)
            } else loadGoodInfo(ean = eanWithoutWeight)
        } ?: loadGoodInfo(ean = eanWithoutWeight)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { foundGood ->
            if (foundGood.isDataLoaded || foundGood.isCounted) {
                lastSuccessSearchNumber.value = material
                setFoundGood(foundGood)
            } else loadGoodInfo(material = material)
        } ?: loadGoodInfo(material = material)
    }

    private fun setFoundGood(foundGood: GoodOpen) {
        manager.updateCurrentGood(foundGood)
        setScreenStatus(foundGood)
        updateProducers(foundGood.producers)
        setDefaultQuantity(foundGood)
    }

    private fun setDefaultQuantity(good: GoodOpen) {
        if (good.isCounted) {
            quantityField.value = good.getQuantity().dropZeros()
        } else if (good.kind == GoodKind.COMMON) {
            if (good.commonUnits != good.convertingUnits) {
                quantityField.value = (scanCodeInfo?.getConvertedQuantity(good.innerQuantity)
                        ?: 0.0).dropZeros()
            } else {
                if (isEanLastScanned) {
                    quantityField.value = "1"
                }
            }
        }
    }

    private fun setScreenStatus(good: GoodOpen) {
        good.apply {
            screenStatus.value = if (isCounted) {
                ScreenStatus.COUNTED
            } else {
                when (kind) {
                    GoodKind.COMMON -> ScreenStatus.COMMON
                    GoodKind.ALCOHOL -> ScreenStatus.ALCOHOL
                    GoodKind.EXCISE -> ScreenStatus.EXCISE
                }
            }
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
                    taskType = task.value?.type?.code.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { goodInfo ->
                viewModelScope.launch {
                    if (manager.isGoodCorrespondToTask(goodInfo)) {
                        if (manager.isGoodCanBeAdded(goodInfo)) {
                            isEanLastScanned = ean != null
                            isExistUnsavedData = true
                            addGood(goodInfo = goodInfo, number = ean ?: (material.orEmpty()))
                        } else {
                            goBackFromScreen()
                            navigator.showGoodCannotBeAdded()
                        }
                    } else {
                        goBackFromScreen()
                        navigator.showNotMatchTaskSettingsAddingNotPossible()
                    }
                }
            }
        }
    }

    private fun goBackFromScreen() {
        if (manager.searchGoodFromList) {
            manager.clearSearchFromListParams()
        }

        navigator.goBack()
    }

    private fun addGood(goodInfo: GoodInfoResult, number: String) {
        viewModelScope.launch {
            goodInfo.apply {
                val commonUnits = database.getUnitsByCode(materialInfo.commonUnitsCode)
                val convertingUnits = database.getUnitsByCode(materialInfo.convertingUnitsCode)
                val innerQuantity = materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0
                val convertingInfo = if (commonUnits != convertingUnits) " (${commonUnits.name} = ${innerQuantity.dropZeros()} ${convertingUnits.name})" else ""

                good.value = GoodOpen(
                        ean = eanInfo.ean,
                        material = materialInfo.material,
                        name = materialInfo.name,
                        section = materialInfo.section,
                        matrix = getMatrixType(goodInfo.materialInfo.matrix),
                        kind = getGoodKind(),
                        commonUnits = commonUnits,
                        convertingUnits = convertingUnits,
                        innerQuantity = materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0,
                        convertingInfo = convertingInfo,
                        isDataLoaded = true,
                        provider = task.value!!.provider,
                        producers = producers
                )
            }

            good.value?.let { good ->
                lastSuccessSearchNumber.value = number
                updateProducers(good.producers)
                setScreenStatus(good)
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

            scanInfoNetRequest(ScanInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = good.value?.material.orEmpty(),
                    markNumber = number,
                    mode = ScanInfoMode.MARK.mode,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleLoadMarkInfoResult(result, number)
            }
        }
    }

    private fun handleLoadMarkInfoResult(result: ScanInfoResult, number: String) {
        viewModelScope.launch {
            result.status.let { status ->
                when (status) {
                    MarkStatus.OK.code, MarkStatus.BAD.code -> addMarkInfo(number, result)
                    MarkStatus.UNKNOWN.code -> {
                        database.getAlcoCodeInfoList(number.extractAlcoCode()).let { alcoCodeInfoList ->
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
                    }
                    else -> navigator.openAlertScreen(result.statusDescription)
                }
            }
        }
    }

    private fun addMarkInfo(number: String, scanInfo: ScanInfoResult) {
        clearSearchFromListParams()
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        scanInfoResult.value = scanInfo
        quantityField.value = "1"

        when (number.length) {
            Constants.MARK_150 -> {
                screenStatus.value = ScreenStatus.MARK_150
                updateProducers(scanInfo.producers.toMutableList())
                date.value = scanInfo.producedDate
            }
            Constants.MARK_68 -> {
                screenStatus.value = ScreenStatus.MARK_68
            }
        }
    }

    private fun addPartInfo(number: String, scanInfo: ScanInfoResult) {
        screenStatus.value = ScreenStatus.PART
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        scanInfoResult.value = scanInfo
        quantityField.value = "1"
        updateProducers(scanInfo.producers.toMutableList())
    }

    private fun loadBoxInfo(number: String) {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            scanInfoNetRequest(ScanInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = good.value?.material.orEmpty(),
                    boxNumber = number,
                    mode = ScanInfoMode.BOX.mode,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleLoadBoxInfoResult(result, number)
            }
        }
    }

    private fun handleLoadBoxInfoResult(result: ScanInfoResult, number: String) {
        viewModelScope.launch {
            when (result.status) {
                BoxStatus.OK.code -> addBoxInfo(number, result)
                else -> navigator.openAlertScreen(result.statusDescription)
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

    private fun addBoxInfo(number: String, scanInfo: ScanInfoResult) {
        screenStatus.value = ScreenStatus.BOX
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        scanInfoResult.value = scanInfo
        quantityField.value = scanInfo.marks.size.toString()
    }

    private suspend fun checkPart(): Either<Failure, ScanInfoResult> {
        navigator.showProgressLoadingData()

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
            changedGood.isCounted = true
            val mark = Mark(
                    number = lastSuccessSearchNumber.value.orEmpty(),
                    material = changedGood.material,
                    isBadMark = scanInfoResult.value?.status == MarkStatus.BAD.code,
                    providerCode = changedGood.provider.code
            )
            Logg.d { "--> add mark = $mark" }
            changedGood.addMark(mark)

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addPart() {
        good.value?.let { changedGood ->
            changedGood.isCounted = true
            val part = Part(
                    number = lastSuccessSearchNumber.value.orEmpty(),
                    material = changedGood.material,
                    quantity = quantity.value ?: 0.0,
                    providerCode = changedGood.provider.code,
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
            changedGood.isCounted = true
            scanInfoResult.value?.marks?.let { marks ->
                marks.forEach { mark ->
                    val markFromBox = Mark(
                            number = mark.number,
                            material = changedGood.material,
                            boxNumber = lastSuccessSearchNumber.value.orEmpty(),
                            isBadMark = mark.isBadMark.isNotEmpty(),
                            providerCode = changedGood.provider.code
                    )
                    Logg.d { "--> add mark from box = $markFromBox" }
                    changedGood.addMark(markFromBox)
                }
            }

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun clearSearchFromListParams() {
        manager.searchGoodFromList = false
        manager.searchNumber = ""
    }

    /**
    Обработка нажатий кнопок
     */

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
        good.value?.let {
            manager.updateCurrentGood(it)
            navigator.openGoodDetailsOpenScreen()
        }
    }

    fun onClickMissing() {
        good.value?.let { changedGood ->
            changedGood.isMissing = true
            manager.updateCurrentGood(changedGood)
        }

        navigator.goBack()
    }

    fun onClickApply() {
        when (screenStatus.value) {
            ScreenStatus.ALCOHOL, ScreenStatus.PART -> {
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
    }

}