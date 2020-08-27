package com.lenta.bp12.features.create_task.marked_good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.MarkStatus
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.actionByNumber
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkStatus
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.MarkCartonBoxGoodInfoNetRequest
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.ScanInfoResult
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestParams
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.isTobaccoCartonMark
import com.lenta.shared.utilities.isTobaccoPackMark
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

    /** "ZMP_UTZ_BKS_05_V001"
     * Получение данных товара по ШК / SAP-коду
     */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var scanInfoNetRequest: ScanInfoNetRequest

    /** ZMP_UTZ_WOB_07_V001
     * «Получение данных по марке/блоку/коробке/товару из ГМ»
     */
    @Inject
    lateinit var markCartonBoxGoodInfoNetRequest: MarkCartonBoxGoodInfoNetRequest

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

    val tempMarks = MutableLiveData(mutableListOf<Mark>())

    /**
    Ввод количества
     */

    val quantityField by unsafeLazy {
        tempMarks.switchMap {
            liveData {
                val size = "${it.size}"
                emit(size)
            }
        }
    }

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
        good.combineLatest(quantity).map {
            it?.let {
                task.value?.let { task ->
                    getBasket()?.let { basket ->
                        "${task.baskets.indexOf(basket) + 1}"
                    } ?: "${task.baskets.size + 1}"
                }.orEmpty()
            }
        }
    }

    private val basketQuantity by lazy {
        good.combineLatest(quantity).map {
            it?.let {
                val good = it.first
                val enteredQuantity = it.second

                getBasket()?.getQuantityOfGood(good)?.sumWith(enteredQuantity)
                        ?: enteredQuantity
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


    /**
    МРЦ
     */

    val mrc by unsafeLazy {
        good.map {
            it?.let { good ->
                val mrc = good.maxRetailPrice
                if (mrc.isNotEmpty()) "${it.maxRetailPrice} ${resource.rub}"
                else ""
            }
        }
    }

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
                .map {
                    it?.let {
                        val enteredQuantity = it.first.first.second
                        val totalQuantity = it.first.second
                        val basketQuantity = it.second

                        val isEnteredQuantityNotZero = enteredQuantity != 0.0
                        val isTotalQuantityMoreThenZero = totalQuantity > 0.0

                        isEnteredQuantityNotZero && isTotalQuantityMoreThenZero && basketQuantity > 0.0
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
            good.value?.let {
                val size = tempMarks.value?.size
                if (size != null && size > 0) isExistUnsavedData = true
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
        good.value?.let {
            launchUITryCatch {
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
                    funcForBox = ::loadBoxInfo,
                    funcForMark = ::checkMark,
                    funcForNotValidBarFormat = {
                        goBackIfSearchFromList()
                        navigator.showIncorrectEanFormat()
                    }
            )
        }
    }

    private fun checkMark(number: String) {
        when (number.length) {
            Constants.MARK_TOBACCO_PACK_29 -> {
                if (isTobaccoPackMark(number))
                    navigator.showCantScanPackAlert()
            }
            in Constants.TOBACCO_MARK_BLOCK_OR_BOX_RANGE_30_44 -> {
                if (isTobaccoCartonMark(number)) {
                    openMarkedGoodWithCarton(number)
                }
            }
            else -> {
                if (isTobaccoCartonMark(number)) {
                    openMarkedGoodWithCarton(number)
                } else {
                    navigator.showIncorrectEanFormat()
                }
            }
        }
    }

    private fun openMarkedGoodWithCarton(number: String) {
        val regex = Regex(Constants.TOBACCO_MARK_CARTON_REGEX_PATTERN).find(number)
        regex?.let {
            val (_, gtin, _, mrc, _, _) = it.destructured // blockBarcode, gtin, serial, mrc, verificationKey, other
            val container = Triple(number, gtin, Mark.Container.CARTON)
            getGoodByEan(gtin, container, mrc)
        }
    }

    private fun getGoodByEan(ean: String, container: Triple<String, String, Mark.Container>? = null, mrc: String = "") {
        manager.findGoodByEan(ean)?.let { foundGood ->
            lastSuccessSearchNumber = ean
            isEanLastScanned = true
            setFoundGood(foundGood)
        } ?: loadGoodInfoByEan(ean, container, mrc)
    }

    private fun setFoundGood(foundGood: GoodCreate, container: Triple<String, String, Mark.Container>? = null) {
        if (container != null) {
            val params = getMarkParams(foundGood, container)
            checkMarkNetRequest(params, foundGood)
        } else {
            manager.updateCurrentGood(foundGood)
            navigator.openMarkedGoodInfoCreateScreen(listOf())
            navigator.showForGoodNeedScanFirstMark()
        }

        manager.updateCurrentGood(foundGood)

        Logg.d { "--> found good: $foundGood" }
    }

    private fun checkMarkNetRequest(params: MarkCartonBoxGoodInfoNetRequestParams, foundGood: GoodCreate) {
        launchUITryCatch {
            val request = markCartonBoxGoodInfoNetRequest(params)
            request.either(
                    fnL = ::handleFailure,
                    fnR = { result ->
                        handleCheckMarkNetRequestResult(result, foundGood)
                    }
            )
        }
    }

    private fun handleCheckMarkNetRequestResult(result: MarkCartonBoxGoodInfoNetRequestResult, foundGood: GoodCreate) {
        val status = result.getMarkStatus()
        val marks = result.marks
        when (status) {
            MarkStatus.GOOD_CARTON -> {
                marks?.let {
                    val mappedMarks = marks.map {
                        Mark(
                                number = it.markNumber,
                                packNumber = it.cartonNumber,
                                maxRetailPrice = foundGood.maxRetailPrice
                        )
                    }
                    tempMarks.value?.let{
                        val list = it
                        mappedMarks.forEach { mark ->
                            if (list.contains(mark).not()) {
                                list.add(mark)
                                isExistUnsavedData = true
                            }
                        }
                        tempMarks.value = list
                    }
                }
            }
            MarkStatus.GOOD_MARK -> {
                marks?.let {
                    val mappedMarks = marks.map {
                        Mark(
                                number = it.markNumber
                        )
                    }
                    tempMarks.value?.addAll(mappedMarks)
                    isExistUnsavedData = true
                }
            }
            MarkStatus.GOOD_BOX -> {
                marks?.let {
                    val mappedMarks = marks.map {
                        Mark(
                                number = it.markNumber,
                                boxNumber = it.boxNumber,
                                maxRetailPrice = foundGood.maxRetailPrice
                        )
                    }
                    tempMarks.value?.addAll(mappedMarks)
                    isExistUnsavedData = true
                }
            }
            else -> {
                navigator.openAlertScreen(
                        Failure.MessageFailure(
                                message = result.markStatusText
                        )
                )
            }
        }
    }



    private fun getMarkParams(foundGood: GoodCreate, container: Triple<String, String, Mark.Container>): MarkCartonBoxGoodInfoNetRequestParams {
        return when (container.third) {
            Mark.Container.CARTON -> {
                MarkCartonBoxGoodInfoNetRequestParams(
                        cartonNumber = container.first,
                        goodNumber = foundGood.material,
                        markType = foundGood.markType.name
                )
            }
            Mark.Container.BOX -> {
                MarkCartonBoxGoodInfoNetRequestParams(
                        boxNumber = container.first,
                        goodNumber = foundGood.material,
                        markType = foundGood.markType.name
                )
            }
            Mark.Container.SHOE -> {
                MarkCartonBoxGoodInfoNetRequestParams(
                        markNumber = container.first,
                        goodNumber = foundGood.material,
                        markType = foundGood.markType.name
                )
            }
        }
    }

    private fun loadGoodInfoByEan(ean: String, container: Triple<String, String, Mark.Container>? = null, mrc: String = "") {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    ean = ean,
                    taskType = task.value?.type?.code.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) {
                handleLoadGoodInfoResult(it, container, mrc)
            }
        }
    }

    private fun handleLoadGoodInfoResult(
            result: GoodInfoResult,
            container: Triple<String, String, Mark.Container>? = null,
            mrc: String = "") {
        launchUITryCatch {
            if (manager.isGoodCanBeAdded(result)) {
                setGood(result, container, mrc)
            } else {
                manager.clearSearchFromListParams()
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

    private fun setGood(
            result: GoodInfoResult,
            container: Triple<String, String, Mark.Container>? = null,
            mrc: String = "") {
        launchUITryCatch {
            with(result) {
                task.value?.let { task ->
                    val taskType = task.type
                    val goodEan = eanInfo?.ean.orEmpty()
                    val umrez = eanInfo?.umrez?.toDoubleOrNull().orIfNull { 1.0 }
                    val formattedMrc = getFormattedMrc(mrc, umrez)

                    val good = GoodCreate(
                            ean = goodEan,
                            eans = database.getEanListByMaterialUnits(
                                    material = materialInfo?.material.orEmpty(),
                                    unitsCode = materialInfo?.commonUnitsCode.orEmpty()
                            ),
                            material = materialInfo?.material.orEmpty(),
                            name = materialInfo?.name.orEmpty(),
                            kind = getGoodKind(),
                            type = materialInfo?.goodType.takeIf { taskType.isDivByGoodType }.orEmpty(),
                            control = getControlType(),
                            section = materialInfo?.section.takeIf { taskType.isDivBySection }.orEmpty(),
                            matrix = getMatrixType(materialInfo?.matrix.orEmpty()),
                            commonUnits = database.getUnitsByCode(materialInfo?.commonUnitsCode.orEmpty()),
                            innerUnits = database.getUnitsByCode(materialInfo?.innerUnitsCode.orEmpty()),
                            innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull()
                                    ?: 1.0,
                            providers = providers?.toMutableList().takeIf { taskType.isDivByProvider }.orEmpty().toMutableList(),
                            producers = producers?.toMutableList().orEmpty().toMutableList(),
                            volume = materialInfo?.volume?.toDoubleOrNull() ?: 0.0,
                            markType = getMarkType(),
                            maxRetailPrice = formattedMrc)

                    setFoundGood(good, container)
                }.orIfNull {
                    Logg.e { "task null" }
                    navigator.showInternalError(resource.taskNotFoundErrorMsg)
                }
            }

            good.value?.let { good ->
                lastSuccessSearchNumber = container?.first.orEmpty()

                Logg.d { "--> added good: $good" }
            }

            manager.clearSearchFromListParams()
        }
    }

    private fun getFormattedMrc(mrc: String, umrez: Double): String {
        val wholeMrc = mrc.toDoubleOrNull()
        return wholeMrc?.let {
            val partedMrc = wholeMrc.div(umrez)
            val partedMrcInRub = partedMrc.div(100)
            "$partedMrcInRub"
        } ?: ""
    }

    private fun loadBoxInfo(number: String) {

    }


    private fun getBasket(): Basket? {
        return manager.getBasket(ProviderInfo.getEmptyCode())
    }

    private suspend fun saveChanges() {
        good.value?.let { good ->
            manager.saveGoodInTask(good)
            isExistUnsavedData = false
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }

        addMarks()
    }

    private suspend fun addMarks() {
        good.value?.let { changedGood ->
            tempMarks.value?.forEach { mark ->
                manager.addGoodToBasketWithMark(
                        good = changedGood,
                        mark = mark,
                        provider = ProviderInfo.getEmptyProvider()
                )
            }
        }
    }

    fun updateData() {
        val good = good.value
        if (manager.isWasAddedProvider && good != null) {
            manager.isWasAddedProvider = false
        }
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
            scanInfoResult.value = null
            tempMarks.value = mutableListOf()
        }
    }

    fun onClickDetails() {
        manager.updateCurrentGood(good.value)
        navigator.openGoodDetailsCreateScreen()
    }

    fun onClickApply() {
        saveChangesAndExit()
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

    fun onClickItemPosition(position: Int) {

    }

}