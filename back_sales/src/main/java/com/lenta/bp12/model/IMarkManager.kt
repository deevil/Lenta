package com.lenta.bp12.model


import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.create_task.marked_good_info.GoodProperty
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.model.pojo.open_task.GoodOpen
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkStatus
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.MarkCartonBoxGoodInfoNetRequest
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestParams
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.PropertiesInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.*
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.unsafeLazy
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

interface IMarkManager {

    fun setWorkType(workType: WorkType)
    suspend fun checkMark(number: String, workType: WorkType): MarkScreenStatus
    fun handleYesDeleteMappedMarksFromTempCallBack()
    fun getMarkFailure(): Failure
    fun getTempMarks(): MutableList<Mark>
    fun getProperties(): MutableList<GoodProperty>
    fun getCreatedGoodForError(): Good?
    fun getInternalErrorMessage(): String
    suspend fun loadBoxInfo(number: String): MarkScreenStatus
    fun onRollback()
    fun clearData()
}

class MarkManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val sessionInfo: ISessionInfo,
        private val createManager: ICreateTaskManager,
        private val openManager: IOpenTaskManager,
        private val resource: IResourceManager,
        private val goodInfoNetRequest: GoodInfoNetRequest,
        private val markCartonBoxGoodInfoNetRequest: MarkCartonBoxGoodInfoNetRequest
) : IMarkManager {

    /**
     * Все сканированные марки хранятся в этом списке до нажатия кнопки применить.
     * После нажатия применить все марки обрабатываются менедежером по корзинам и сохраняется в задании.
     * */
    private var tempMarks: MutableList<Mark> = mutableListOf()

    /**
     * Последние отсканированные марки, сохраняенные в менеджере, для удаления их из общего списка по кнопке Откат
     * */
    private var lastScannedMarks: List<Mark> = listOf()

    /**
     * Последние отсканированные марки для удаления при дублировании
     * */
    private var mappedMarks: List<Mark> = listOf()

    private var properties = mutableListOf<GoodProperty>()

    private var isExistUnsavedData = false

    private var failure: Failure = Failure.ServerError

    private val createdGoodToShowError by unsafeLazy {
        MutableLiveData<Good>()
    }

    private var internalErrorMessage = ""

    private var workType: WorkType = WorkType.OPEN

    override fun setWorkType(workType: WorkType) {
        this.workType = workType
    }

    /**
     * Метод уже конкретно определяет Марка обуви, Коробка, или Блок
     * */
    override suspend fun checkMark(number: String, workType: WorkType): MarkScreenStatus {
        this.workType = workType
        return if (isShoesMark(number)) {
            openMarkedGoodWithShoe(number)
        } else when (number.length) {
            in Constants.TOBACCO_BOX_MARK_RANGE_21_28 -> {
                loadBoxInfo(number)
            }
            Constants.MARK_TOBACCO_PACK_29 -> {
                if (isTobaccoPackMark(number))
                    return MarkScreenStatus.CANT_SCAN_PACK
                else {
                    loadBoxInfo(number)
                }
            }
            in Constants.TOBACCO_MARK_BLOCK_OR_BOX_RANGE_30_44 -> {
                if (isTobaccoCartonMark(number)) {
                    openMarkedGoodWithCarton(number)
                } else {
                    loadBoxInfo(number)
                }
            }
            else -> {
                if (isTobaccoCartonMark(number)) {
                    openMarkedGoodWithCarton(number)
                } else {
                    MarkScreenStatus.INCORRECT_EAN_FORMAT
                }
            }
        }
    }

    /**
     * Метод вычленяет регулярным выражением шк, гтин и мрц из марки блока
     * */
    private suspend fun openMarkedGoodWithCarton(number: String): MarkScreenStatus {
        val good = when (workType) {
            WorkType.CREATE -> createManager.currentGood
            WorkType.OPEN -> openManager.currentGood
        }
        Logg.e { good.value.toString() }

        val regex = Regex(Constants.TOBACCO_MARK_CARTON_REGEX_PATTERN).find(number)
        return regex?.let {
            val (blocBarcode, gtin, _, mrc, _, _) = it.destructured // blockBarcode, gtin, serial, mrc, verificationKey, other
            val container = Pair(blocBarcode, Mark.Container.CARTON)

            getGoodByEan(gtin, container, mrc)
        }.orIfNull {
            internalErrorMessage = "carton regex null"
            Logg.e { internalErrorMessage }
            MarkScreenStatus.INTERNAL_ERROR
        }
    }


    override suspend fun loadBoxInfo(number: String): MarkScreenStatus {
        val goodFromManager = when (workType) {
            WorkType.CREATE -> createManager.currentGood
            WorkType.OPEN -> openManager.currentGood
        }

        return goodFromManager.value?.let { good ->
            val container = Pair(number, Mark.Container.BOX)
            val params = getMarkParams(good, container)
            checkMarkNetRequest(params, good)
        }.orIfNull {
            internalErrorMessage = resource.goodNotFoundErrorMsg
            Logg.e { internalErrorMessage }
            MarkScreenStatus.INTERNAL_ERROR
        }

    }

    override fun onRollback() {
        val good = when (workType) {
            WorkType.OPEN -> {
                openManager.currentGood.value
            }
            WorkType.CREATE -> {
                createManager.currentGood.value
            }
        }
        good?.let {
            tempMarks.removeAll(lastScannedMarks)
        }
    }

    override fun clearData() {
        tempMarks.clear()
        properties.clear()
        lastScannedMarks = emptyList()
        mappedMarks = emptyList()
        createdGoodToShowError.value = null
        internalErrorMessage = ""
        isExistUnsavedData = false
    }

    private suspend fun openMarkedGoodWithShoe(number: String): MarkScreenStatus {
        val regex = Regex(Constants.SHOES_MARK_REGEX_PATTERN).find(number)
        return regex?.let {
            val (barcode, gtin, _, _, _, _) = it.destructured // barcode, gtin, serial, tradeCode, verificationKey, verificationCode
            val container = Pair(barcode, Mark.Container.SHOE)
            getGoodByEan(gtin, container)
        }.orIfNull {
            internalErrorMessage = "shoe regex null"
            Logg.e { internalErrorMessage }
            MarkScreenStatus.INTERNAL_ERROR
        }
    }


    /**
     * Метод определяет есть ли такой товар в менеджере или нет,
     * Если есть, то откроет его, если нет то создаст
     * */
    private suspend fun getGoodByEan(
            ean: String,
            container: Pair<String, Mark.Container>? = null,
            mrc: String = ""
    ): MarkScreenStatus {
        when (workType) {
            WorkType.CREATE -> {
                return createManager.findGoodByEan(ean)?.let { foundGood ->
                    setFoundGoodCreate(foundGood)
                } ?: loadGoodInfoByEan(ean, container, mrc)
            }
            WorkType.OPEN -> {
                openManager.findGoodByEan(ean)?.let { foundGood ->
                    return setFoundGoodOpen(foundGood, container)
                }

                val task = openManager.currentTask
                return if (task.value?.isStrict == false) {
                    loadGoodInfoByEan(ean, container, mrc)
                } else {
                    MarkScreenStatus.GOOD_IS_MISSING_IN_TASK
                }
            }
        }
    }

    /**
     * Метод делает запрос ФМ "ZMP_UTZ_BKS_05_V001" о товаре по ШК
     * */
    private suspend fun loadGoodInfoByEan(
            ean: String,
            container: Pair<String, Mark.Container>? = null,
            mrc: String = ""
    ): MarkScreenStatus {
        when (workType) {
            WorkType.CREATE -> {
                val task = createManager.currentTask
                var screenStatus = MarkScreenStatus.FAILURE
                goodInfoNetRequest(GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        ean = ean,
                        taskType = task.value?.type?.code.orEmpty()
                )).either(::handleFailure) {
                    screenStatus = handleLoadGoodInfoResult(it, container, mrc)
                    Unit
                }
                return screenStatus
            }
            WorkType.OPEN -> {
                val task = openManager.currentTask
                var screenStatus = MarkScreenStatus.FAILURE
                goodInfoNetRequest(GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        ean = ean,
                        taskType = task.value?.type?.code.orEmpty()
                )).either(::handleFailure) {
                    screenStatus = handleLoadGoodInfoResult(it, container, mrc)
                    Unit
                }
                return screenStatus
            }
        }


    }

    private fun handleFailure(failure: Failure): MarkScreenStatus {
        this.failure = failure
        return MarkScreenStatus.FAILURE
    }

    /**
     * Метод обрабатывает результат запроса фм "ZMP_UTZ_BKS_05_V001"
     * */
    private fun handleLoadGoodInfoResult(
            result: GoodInfoResult,
            container: Pair<String, Mark.Container>? = null,
            mrc: String = ""
    ): MarkScreenStatus {

        return runBlocking {
            if (openManager.isGoodCanBeAdded(result)) {
                setGood(result, container, mrc)
            } else {
                openManager.clearSearchFromListParams()
                MarkScreenStatus.GOOD_CANNOT_BE_ADDED
            }
        }
    }

    /**
     * Метод создает товар по полученным данным и устанавливает ему мрц
     * */
    private suspend fun setGood(
            result: GoodInfoResult,
            container: Pair<String, Mark.Container>? = null,
            mrc: String = ""
    ): MarkScreenStatus {
        when (workType) {
            WorkType.OPEN -> {
                openManager.clearSearchFromListParams()
                val taskFromManager = openManager.currentTask
                val goodFromManager = openManager.currentGood
                return with(result) {
                    taskFromManager.value?.let { task ->
                        goodFromManager.value?.let { good ->
                            val goodEan = eanInfo?.ean.orEmpty()
                            val umrez = eanInfo?.umrez?.toDoubleOrNull().orIfNull { DEFAULT_UMREZ }
                            val formattedMrc = getFormattedMrc(mrc, umrez)
                            val markType = getMarkType()

                            val createdGood = GoodOpen(
                                    ean = goodEan,
                                    material = materialInfo?.material.orEmpty(),
                                    name = materialInfo?.name.orEmpty(),
                                    kind = getGoodKind(),
                                    control = getControlType(),
                                    section = materialInfo?.section.orEmpty(),
                                    matrix = getMatrixType(materialInfo?.matrix.orEmpty()),
                                    commonUnits = database.getUnitsByCode(materialInfo?.commonUnitsCode.orEmpty()),
                                    innerUnits = database.getUnitsByCode(materialInfo?.innerUnitsCode.orEmpty()),
                                    innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull()
                                            ?: DEFAULT_INNER_QUALITY_VALUE,
                                    provider = task.provider,
                                    producers = producers?.toMutableList().orEmpty().toMutableList(),
                                    volume = materialInfo?.volume?.toDoubleOrNull() ?: DEFAULT_VOLUME_VALUE,
                                    markType = markType,
                                    markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                                    maxRetailPrice = formattedMrc)

                            if (database.isMarkTypeInDatabase(good.markType)) {
                                if (createdGood.markType == good.markType) {
                                    Logg.e {
                                        task.mrcList.toString()
                                    }
                                    if (good.markType == MarkType.TOBACCO
                                            && task.mrcList.none { it.maxRetailPrice == formattedMrc }
                                    ) {
                                        createdGoodToShowError.value = createdGood
                                        MarkScreenStatus.MRC_NOT_SAME
                                    } else {
                                        setFoundGoodOpen(createdGood, container)
                                    }
                                } else {
                                    MarkScreenStatus.NOT_MARKED_GOOD
                                }
                            } else {
                                MarkScreenStatus.NO_MARKTYPE_IN_SETTINGS
                            }
                        }.orIfNull {
                            internalErrorMessage = resource.goodNotFoundErrorMsg
                            Logg.e { internalErrorMessage }
                            MarkScreenStatus.INTERNAL_ERROR
                        }
                    }.orIfNull {
                        internalErrorMessage = resource.taskNotFoundErrorMsg
                        Logg.e { internalErrorMessage }
                        MarkScreenStatus.INTERNAL_ERROR
                    }
                }
            }
            WorkType.CREATE -> {
                createManager.clearSearchFromListParams()
                val taskFromManager = createManager.currentTask
                val goodFromManager = createManager.currentGood
                val good = goodFromManager.value
                return with(result) {
                    taskFromManager.value?.let { task ->
                        val taskType = task.type
                        val goodEan = eanInfo?.ean.orEmpty()
                        val umrez = eanInfo?.umrez?.toDoubleOrNull().orIfNull { DEFAULT_UMREZ }
                        val formattedMrc = getFormattedMrc(mrc, umrez)
                        val markType = getMarkType()
                        val createdGood = GoodCreate(
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
                                providers = providers?.takeIf { taskType.isDivByProvider }.orEmpty().toMutableList(),
                                producers = producers.orEmpty().toMutableList(),
                                volume = materialInfo?.volume?.toDoubleOrNull() ?: 0.0,
                                markType = markType,
                                markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                                maxRetailPrice = formattedMrc
                        )
                        if (good == null || createdGood.markType == good.markType) {
                            setFoundGoodCreate(createdGood, container)
                        } else {
                            MarkScreenStatus.INCORRECT_EAN_FORMAT
                        }

                    }.orIfNull {
                        internalErrorMessage = resource.taskNotFoundErrorMsg
                        Logg.e { internalErrorMessage }
                        MarkScreenStatus.INTERNAL_ERROR
                    }
                }
            }
        }
    }

    /**
     * Форматирует мрц, если он есть
     * Он приходит в копейках и общий для всех товаров в блоке/коробке.
     * Умрез - количество в блоке/коробке, приходит из ФМ. Делим на него и делим на 100 чтобы в рублях получить
     * */
    private fun getFormattedMrc(mrc: String, umrez: Double): String {
        val wholeMrc = mrc.toDoubleOrNull()
        return wholeMrc?.let {
            val partedMrc = wholeMrc.div(umrez)
            val partedMrcInRub = partedMrc.div(100).dropZeros()
            partedMrcInRub
        } ?: ""
    }

    /**
     * Если в метод передали контейнер, то это значит что отсканирована марка, и надо выполнять дальнейшие проверки
     * Если нет, то значит отсканирован штрихкод товара и его надо показать
     * */
    private suspend fun setFoundGoodOpen(
            foundGood: GoodOpen,
            container: Pair<String, Mark.Container>? = null
    ): MarkScreenStatus {
        openManager.updateCurrentGood(foundGood)

        Logg.d { "--> found good: $foundGood" }

        return if (container != null) {
            val params = getMarkParams(foundGood, container)
            checkMarkNetRequest(params, foundGood)
        } else {
            openManager.updateCurrentGood(foundGood)
            MarkScreenStatus.OK_BUT_NEED_TO_SCAN_MARK
        }
    }

    /**
     * Если в метод передали контейнер, то это значит что отсканирована марка, и надо выполнять дальнейшие проверки
     * Если нет, то значит отсканирован штрихкод товара и его надо показать
     * */
    private suspend fun setFoundGoodCreate(
            foundGood: GoodCreate,
            container: Pair<String, Mark.Container>? = null
    ): MarkScreenStatus {
        createManager.updateCurrentGood(foundGood)

        Logg.d { "--> found good: $foundGood" }

        return if (container != null) {
            val params = getMarkParams(foundGood, container)
            checkMarkNetRequest(params, foundGood)
        } else {
            createManager.updateCurrentGood(foundGood)
            MarkScreenStatus.OK_BUT_NEED_TO_SCAN_MARK
        }

    }

    /**
     * Метод вызывает ФМ ZMP_UTZ_WOB_07_V001
     * */
    private suspend fun checkMarkNetRequest(
            params: MarkCartonBoxGoodInfoNetRequestParams,
            foundGood: Good
    ): MarkScreenStatus {

        val request = markCartonBoxGoodInfoNetRequest(params)
        var screenStatus: MarkScreenStatus = MarkScreenStatus.FAILURE
        request.either(
                fnL = ::handleFailure,
                fnR = { result ->
                    screenStatus = handleCheckMarkNetRequestResult(result, foundGood)
                    Unit
                }
        )
        return screenStatus
    }


    /**
     * Метод обрабатывает результат ФМ ZMP_UTZ_WOB_07_V001
     * В зависимости от типа сканированного товара (марка, коробка, блок)
     * создает марку. Затем если она присутствует показывает сообщение что она уже присутствует
     * и по нажатию Да, удаляет ее( их если коробка/блок), если нет то добавляет в общий список.
     * */
    private fun handleCheckMarkNetRequestResult(result: MarkCartonBoxGoodInfoNetRequestResult, foundGood: Good): MarkScreenStatus {
        val status = result.getMarkStatus()
        val marks = result.marks
        val properties = result.properties
        return marks?.let { resultMarks ->
            tempMarks.let { localTempMarks ->
                when (status) {
                    MarkStatus.GOOD_CARTON -> {
                        val mappedMarks = resultMarks.map {
                            Mark(
                                    number = it.markNumber.orEmpty(),
                                    packNumber = it.cartonNumber.orEmpty(),
                                    maxRetailPrice = foundGood.maxRetailPrice
                            )
                        }
                        addOrDeleteMarksFromTemp(
                                restProperties = properties,
                                localTempMarks = localTempMarks,
                                mappedMarks = mappedMarks,
                                screenStatus = MarkScreenStatus.CARTON_ALREADY_SCANNED
                        )
                    }
                    MarkStatus.GOOD_MARK -> {
                        val mappedMarks = marks.map {
                            Mark(
                                    number = it.markNumber.orEmpty()
                            )
                        }
                        addOrDeleteMarksFromTemp(
                                restProperties = properties,
                                localTempMarks = localTempMarks,
                                mappedMarks = mappedMarks,
                                screenStatus = MarkScreenStatus.MARK_ALREADY_SCANNED
                        )
                    }
                    MarkStatus.GOOD_BOX -> {
                        val mappedMarks = marks.map {
                            Mark(
                                    number = it.markNumber.orEmpty(),
                                    boxNumber = it.boxNumber.orEmpty(),
                                    maxRetailPrice = foundGood.maxRetailPrice
                            )
                        }
                        addOrDeleteMarksFromTemp(
                                restProperties = properties,
                                localTempMarks = localTempMarks,
                                mappedMarks = mappedMarks,
                                screenStatus = MarkScreenStatus.BOX_ALREADY_SCANNED
                        )
                    }
                    else -> {
                        failure = Failure.MessageFailure(
                                message = result.markStatusText.orIfNull { resource.noStatusMark }
                        )
                        MarkScreenStatus.FAILURE
                    }
                }
            }
        }.orIfNull {
            internalErrorMessage = "marks null"
            Logg.e { internalErrorMessage }
            MarkScreenStatus.INTERNAL_ERROR
        }
    }

    /**
     * Определяет есть ли марка в общем списке, если есть то вызывает сообщение о том что она есть
     * (кнопка да удаляет марки)
     * Если нет то добавляет, и обновляет лайв дату
     * */
    private fun addOrDeleteMarksFromTemp(
            restProperties: List<PropertiesInfo>?,
            localTempMarks: MutableList<Mark>,
            mappedMarks: List<Mark>,
            screenStatus: MarkScreenStatus
    ): MarkScreenStatus {
        return if (localTempMarks.any { tempMark ->
                    mappedMarks.any {
                        tempMark.number == it.number
                    }
                }
        ) {
            this.mappedMarks = mappedMarks
            screenStatus
        } else {
            val restPropertiesMapped = restProperties?.map { propertyFromRest ->
                GoodProperty(
                        gtin = propertyFromRest.ean.orEmpty(),
                        property = propertyFromRest.propertyName.orEmpty(),
                        value = propertyFromRest.propertyValue.orEmpty()
                )
            }.orEmpty()
            properties = properties.union(restPropertiesMapped).toMutableList()
            mappedMarks.forEach { mappedMark ->
                localTempMarks.add(mappedMark)
            }
            tempMarks = localTempMarks
            lastScannedMarks = mappedMarks
            MarkScreenStatus.OK
        }
    }

    override fun getTempMarks(): MutableList<Mark> = tempMarks
    override fun getProperties(): MutableList<GoodProperty> = properties
    override fun getCreatedGoodForError(): Good? {
        return createdGoodToShowError.value?.let {
            return it
        }
    }

    override fun getInternalErrorMessage(): String {
        return internalErrorMessage
    }

    /**
     * Возвращает параметры для запроса ФМ ZMP_UTZ_WOB_07_V001
     * в зависимости от того что мы сканируем
     * */
    private fun getMarkParams(foundGood: Good, container: Pair<String, Mark.Container>): MarkCartonBoxGoodInfoNetRequestParams {
        return when (container.second) {
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

    override fun handleYesDeleteMappedMarksFromTempCallBack() {
        tempMarks.removeAll(mappedMarks)
    }

    override fun getMarkFailure(): Failure {
        return failure
    }

    companion object {
        private const val DEFAULT_UMREZ = 1.0
        private const val DEFAULT_INNER_QUALITY_VALUE = 1.0
        private const val DEFAULT_VOLUME_VALUE = 0.0
    }
}