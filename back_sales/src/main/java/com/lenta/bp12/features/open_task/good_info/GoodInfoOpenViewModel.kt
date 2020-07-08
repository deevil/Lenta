package com.lenta.bp12.features.open_task.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.open_task.GoodOpen
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.*
import com.lenta.bp12.request.pojo.ProducerInfo
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
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.math.BigInteger
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
            good?.getNameWithMaterial()
        }
    }

    private var isExistUnsavedData = false

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

    private var isEanLastScanned = false

    private var scanCodeInfo: ScanCodeInfo? = null

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
            resource.totalWithConvertingInfo(good?.convertingInfo ?: "")
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

    private val selectedProducer by lazy {
        producers.combineLatest(producerPosition).map {
            it?.let {
                val list = it.first
                val position = it.second
                if (list.isNotEmpty()) list[position] else null
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
            ScanNumberType.ALCOHOL, ScanNumberType.PART -> true
            else -> false
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val applyEnabled by lazy {
        scanModeType.combineLatest(quantity).combineLatest(isProducerSelected).combineLatest(isCorrectDate).map {
            it?.let {
                val type = it.first.first.first
                val quantity = it.first.first.second
                val isProducerSelected = it.first.second
                val isDateEntered = it.second

                when (type) {
                    ScanNumberType.COMMON -> quantity > 0.0
                    ScanNumberType.ALCOHOL -> quantity > 0.0 && isProducerSelected && isDateEntered
                    ScanNumberType.EXCISE -> false
                    ScanNumberType.MARK_150 -> true
                    ScanNumberType.MARK_68 -> true
                    ScanNumberType.PART -> isProducerSelected && isDateEntered
                    ScanNumberType.BOX -> isProducerSelected
                    else -> false
                }
            } ?: false
        }
    }

    val detailsVisibility = scanModeType.map { type ->
        when (type) {
            ScanNumberType.MARK_150, ScanNumberType.MARK_68, ScanNumberType.PART -> true
            else -> false
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

    val missingVisibility = scanModeType.map { type ->
        when (type) {
            ScanNumberType.COMMON, ScanNumberType.ALCOHOL, ScanNumberType.EXCISE -> true
            else -> false
        }
    }

    /**
    Блок инициализации
     */

    init {
        viewModelScope.launch {
            checkSearchNumber(manager.searchNumber)

            // todo При открытии загруженного товара этот номер пустой
            // Не отрабатывается блок загрузки и смены состояния экрана
        }
    }

    /**
    Методы
     */

    fun onScanResult(number: String) {
        if (task.value?.isStrict == true && (number.length < Constants.SAP_6 || number.length > Constants.MARK_150)) {
            return
        }

        if (applyEnabled.value!! || good.value!!.kind == GoodKind.EXCISE && (number.length == Constants.MARK_150 || number.length == Constants.MARK_68 || number.length == Constants.BOX_26)) {
            saveChanges()
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
                    Constants.MARK_150 -> {
                        loadMarkInfo(number)
                    }
                    Constants.MARK_68 -> {
                        loadMarkInfo(number)
                    }
                    Constants.BOX_26 -> {
                        loadBoxInfo(number)
                    }
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
            if (foundGood.isDataLoaded) {
                lastSuccessSearchNumber.value = eanWithoutWeight
                setFoundGood(foundGood)
            } else loadGoodInfo(ean = eanWithoutWeight)
        } ?: loadGoodInfo(ean = eanWithoutWeight)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { foundGood ->
            if (foundGood.isDataLoaded) {
                lastSuccessSearchNumber.value = material
                setFoundGood(foundGood)
            } else loadGoodInfo(material = material)
        } ?: loadGoodInfo(material = material)
    }

    private fun setFoundGood(foundGood: GoodOpen) {
        manager.updateCurrentGood(foundGood)
        setScanModeFromGoodKind(foundGood.kind)
        updateProducers(foundGood.producers)
        setDefaultQuantity(foundGood)
    }

    private fun setDefaultQuantity(good: GoodOpen) {
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
                    tkNumber = sessionInfo.market ?: "",
                    ean = ean ?: "",
                    material = material ?: "",
                    taskType = task.value?.type?.code ?: ""
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { goodInfo ->
                viewModelScope.launch {
                    if (manager.isGoodCorrespondToTask(goodInfo)) {
                        if (manager.isGoodCanBeAdded(goodInfo)) {
                            isEanLastScanned = ean != null
                            isExistUnsavedData = true
                            addGood(goodInfo, ean ?: (material ?: ""))
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
                    tkNumber = sessionInfo.market ?: "",
                    material = good.value?.material ?: "",
                    markNumber = number,
                    mode = 1,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { markInfoResult ->
                viewModelScope.launch {
                    markInfoResult.status.let { status ->
                        if (status == MarkStatus.OK.code || status == MarkStatus.BAD.code) {
                            addMarkInfo(number, markInfoResult)
                        } else if (status == MarkStatus.UNKNOWN.code) {
                            val alcoCode = BigInteger(number.substring(7, 19), 36).toString().padStart(19, '0')
                            database.getAlcoCodeInfoList(alcoCode).let { alcoCodeInfoList ->
                                if (alcoCodeInfoList.isNotEmpty()) {
                                    if (alcoCodeInfoList.find { it.material == good.value!!.material } != null) {
                                        addPartInfo(number, markInfoResult)
                                    } else {
                                        navigator.openAlertScreen(resource.alcocodeDoesNotApplyToThisGood())
                                    }
                                } else {
                                    navigator.openAlertScreen(resource.unknownAlcocode())
                                }
                            }
                        } else {
                            navigator.openAlertScreen(markInfoResult.statusDescription)
                        }
                    }
                }
            }
        }
    }

    private fun addMarkInfo(number: String, markInfo: MarkInfoResult) {
        clearSearchFromListParams()
        lastSuccessSearchNumber.value = number
        isExistUnsavedData = true
        markInfoResult.value = markInfo
        quantityField.value = "1"

        when (number.length) {
            Constants.MARK_150 -> {
                scanModeType.value = ScanNumberType.MARK_150
                updateProducers(markInfo.producers.toMutableList())
                date.value = markInfo.producedDate
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
                    tkNumber = sessionInfo.market ?: "",
                    material = good.value?.material ?: "",
                    markNumber = number,
                    mode = 2,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { markInfoResult ->
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

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        if (manager.searchGoodFromList) {
            manager.clearSearchFromListParams()
            navigator.goBack()
        }

        navigator.openAlertScreen(failure)
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
                tkNumber = sessionInfo.market ?: "",
                material = good.value?.material ?: "",
                producerCode = selectedProducer.value?.code ?: "",
                bottledDate = date.value ?: "",
                mode = 3,
                quantity = quantity.value ?: 0.0
        )).also {
            navigator.hideProgress()
        }
    }

    private fun updateProducers(producers: List<ProducerInfo>) {
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
            changedGood.isCounted = true
            changedGood.addPosition(quantity.value!!, changedGood.provider)

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addMark() {
        good.value?.let { changedGood ->
            changedGood.isCounted = true
            changedGood.addMark(Mark(
                    number = lastSuccessSearchNumber.value!!,
                    material = changedGood.material,
                    isBadMark = markInfoResult.value?.status == MarkStatus.BAD.code,
                    providerCode = changedGood.provider.code,
                    producerCode = selectedProducer.value?.code ?: ""
            ))

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addPart() {
        good.value?.let { changedGood ->
            changedGood.isCounted = true
            changedGood.addPart(Part(
                    number = lastSuccessSearchNumber.value!!,
                    material = changedGood.material,
                    quantity = quantity.value!!,
                    units = changedGood.commonUnits,
                    providerCode = changedGood.provider.code,
                    producerCode = selectedProducer.value?.code ?: "",
                    date = date.value!!
            ))

            manager.updateCurrentGood(changedGood)
        }
    }

    private fun addBox() {
        good.value?.let { changedGood ->
            changedGood.isCounted = true
            markInfoResult.value?.marks?.let { marks ->
                marks.forEach { mark ->
                    changedGood.addMark(Mark(
                            number = mark.number,
                            material = changedGood.material,
                            boxNumber = lastSuccessSearchNumber.value!!,
                            isBadMark = mark.isBadMark.isNotEmpty(),
                            providerCode = changedGood.provider.code,
                            producerCode = selectedProducer.value?.code ?: ""
                    ))
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
        good.value?.let { changedGood ->
            when (scanModeType.value) {
                ScanNumberType.MARK_150, ScanNumberType.MARK_68 -> {
                    changedGood.removeMark(lastSuccessSearchNumber.value!!)
                }
                ScanNumberType.BOX -> {
                    markInfoResult.value?.marks?.forEach { mark ->
                        changedGood.removeMark(mark.number)
                    }
                }
            }

            quantityField.value = ""
            markInfoResult.value = null

            manager.updateCurrentGood(changedGood)
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
        when (scanModeType.value) {
            ScanNumberType.ALCOHOL, ScanNumberType.PART -> {
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