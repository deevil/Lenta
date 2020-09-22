package com.lenta.bp12.managers

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.create_task.marked_good_info.GoodProperty
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.MarkScreenStatus
import com.lenta.bp12.model.MarkStatus
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.extentions.isAnyAlreadyIn
import com.lenta.bp12.model.pojo.extentions.mapToMarkList
import com.lenta.bp12.platform.ZERO_VOLUME
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
import com.lenta.shared.platform.constants.Constants.MARK_TOBACCO_PACK_29
import com.lenta.shared.platform.constants.Constants.TOBACCO_BOX_MARK_RANGE_21_28
import com.lenta.shared.platform.constants.Constants.TOBACCO_MARK_BLOCK_OR_BOX_RANGE_30_44
import com.lenta.shared.utilities.*
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.unsafeLazy
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Менеджер ответственный за сканирование марок (не акцизных)
 * главный метод:
 * @see checkMark
 * В него передается код марки, далее он сам ищет товары и обрабатывает и возвращает enum состояния
 * Важно! Самостоятельно он не определяет марка это или штрихкод
 * */
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

    /**
     * Если менеджер вернет MarkScreenStatus.MRC_NOT_SAME то этот товар будет показан на экране ошибки
     */
    private val createdGoodToShowError by unsafeLazy {
        MutableLiveData<Good>()
    }

    /**
     * Если менеджер вернет MarkScreenStatus.MRC_NOT_SAME_IN_BOX то этот товар будет использоват как текущий при нажатии на "Да обновить корзину"
     * */

    private val tempGood by unsafeLazy {
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
            in TOBACCO_BOX_MARK_RANGE_21_28 -> {
                loadBoxInfo(number, workType)
            }
            MARK_TOBACCO_PACK_29 -> {
                if (isCigarettesMark(number))
                    return MarkScreenStatus.CANT_SCAN_PACK
                else {
                    loadBoxInfo(number, workType)
                }
            }
            in TOBACCO_MARK_BLOCK_OR_BOX_RANGE_30_44 -> {
                if (isCigarettesBox(number)) {
                    openMarkedGoodWithCarton(number)
                } else {
                    loadBoxInfo(number, workType)
                }
            }
            else -> {
                if (isCigarettesBox(number)) {
                    openMarkedGoodWithCarton(number)
                } else {
                    MarkScreenStatus.INCORRECT_EAN_FORMAT
                }
            }
        }
    }

    /**
     * Метод вычленяет регулярным выражением шк(barcode), гтин и мрц из марки блока
     * */
    private suspend fun openMarkedGoodWithCarton(number: String): MarkScreenStatus {
        val regex = Regex(Constants.CIGARETTES_BOX_PATTERN).find(number)
        return regex?.let {
            val (blocBarcode, gtin, _, mrc, _, _) = it.destructured // blockBarcode, gtin, serial, mrc, verificationKey, other
            val container = Pair(blocBarcode, Mark.Container.CARTON)
            val ean = gtin.getEANfromGTIN()
            getGoodByEan(ean, container, mrc)
        }.orIfNull {
            internalErrorMessage = "carton regex null"
            Logg.e { internalErrorMessage }
            MarkScreenStatus.INTERNAL_ERROR
        }
    }

    override suspend fun loadBoxInfo(number: String, workType: WorkType): MarkScreenStatus {
        this.workType = workType
        val goodFromManager = chooseGood()

        return goodFromManager?.let { good ->
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
        val good = chooseGood()
        good?.let {
            tempMarks.removeAll(lastScannedMarks)
        }
    }

    override fun clearData() {
        tempMarks.clear()
        properties.clear()
        lastScannedMarks = emptyList()
        //mappedMarks = emptyList()
        createdGoodToShowError.value = null
        internalErrorMessage = ""
        isExistUnsavedData = false
    }

    private suspend fun openMarkedGoodWithShoe(number: String): MarkScreenStatus {
        val regex = Regex(Constants.SHOES_MARK_PATTERN).find(number)
        return regex?.let {
            val (barcode, gtin, _, _, _, _) = it.destructured // barcode, gtin, serial, tradeCode, verificationKey, verificationCode
            val container = Pair(barcode, Mark.Container.SHOE)
            val ean = gtin.getEANfromGTIN()
            getGoodByEan(ean, container)
        }.orIfNull {
            internalErrorMessage = "shoe regex null"
            Logg.e { internalErrorMessage }
            MarkScreenStatus.INTERNAL_ERROR
        }
    }

    private fun String.getEANfromGTIN(): String = if (this.startsWith("0")) {
        this.drop(1)
    } else this

    /**
     * Метод определяет есть ли такой товар в менеджере или нет,
     * Если есть, то откроет его, если нет то создаст
     * */
    private suspend fun getGoodByEan(
            ean: String,
            container: Pair<String, Mark.Container>? = null,
            mrc: String = ""
    ): MarkScreenStatus {

        val formattedMrc = mrc.getFormattedMrc(ean)

        when (workType) {
            WorkType.CREATE -> {
                val good = createManager.findGoodByEanAndMRC(ean, formattedMrc)

                return good?.let { foundGood ->
                    Logg.e { foundGood.maxRetailPrice }
                    setFoundGood(foundGood, container)
                } ?: loadGoodInfoByEan(ean, container, formattedMrc)
            }
            WorkType.OPEN -> {
                val good = openManager.findGoodByEanAndMRC(ean, formattedMrc)

                good?.let { foundGood ->
                    return setFoundGood(foundGood, container)
                }

                val task = openManager.currentTask
                return if (task.value?.isStrict == false) {
                    loadGoodInfoByEan(ean, container, formattedMrc)
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
            formattedMrc: String = ""
    ): MarkScreenStatus {
        return when (workType) {
            WorkType.CREATE -> {
                val task = createManager.currentTask
                val createTaskTypeCode = task.value?.type?.code.orEmpty()
                goodInfoRequest(createTaskTypeCode, ean, container, formattedMrc)
            }
            WorkType.OPEN -> {
                val task = openManager.currentTask
                val openTaskTypeCode = task.value?.type?.code.orEmpty()
                goodInfoRequest(openTaskTypeCode, ean, container, formattedMrc)
            }
        }
    }

    private suspend fun goodInfoRequest(
            taskTypeCode: String,
            ean: String,
            container: Pair<String, Mark.Container>? = null,
            formattedMrc: String = ""
    ): MarkScreenStatus {
        var screenStatus = MarkScreenStatus.FAILURE
        goodInfoNetRequest(GoodInfoParams(
                tkNumber = sessionInfo.market.orEmpty(),
                ean = ean,
                taskType = taskTypeCode
        )).either(::handleFailure) {
            screenStatus = handleLoadGoodInfoResult(it, container, formattedMrc)
            Unit
        }
        return screenStatus
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
            formattedMrc: String = ""
    ): MarkScreenStatus {
        val manager = chooseManager()
        return runBlocking {
            if (manager.isGoodCanBeAdded(result)) {
                setGood(result, container, formattedMrc)
            } else {
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
        return when (workType) {
            WorkType.OPEN -> setOpenGood(result, container, mrc)
            WorkType.CREATE -> setCreateGood(result, container, mrc)
        }
    }

    private suspend fun setCreateGood(
            result: GoodInfoResult,
            container: Pair<String, Mark.Container>? = null,
            mrc: String = ""
    ): MarkScreenStatus {
        val goodFromManager = createManager.currentGood
        val good = goodFromManager.value
        return with(result) {
                val goodEan = eanInfo?.ean.orEmpty()
                val markType = getMarkType()
                val createdGood = Good(
                        ean = goodEan,
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
                        innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull()
                                ?: 1.0,
                        providers = providers.orEmpty().toMutableList(),
                        producers = producers.orEmpty().toMutableList(),
                        volume = materialInfo?.volume?.toDoubleOrNull() ?: ZERO_VOLUME,
                        markType = markType,
                        markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                        maxRetailPrice = mrc,
                        purchaseGroup = materialInfo?.purchaseGroup.orEmpty()
                )

                if(good != null && createdGood.material != good.material) {
                    createdGoodToShowError.value = createdGood
                    MarkScreenStatus.NOT_SAME_GOOD
                }

                if (database.isMarkTypeInDatabase(createdGood.markType)) {
                    if (good == null || createdGood.markType == good.markType) {
                        setFoundGood(createdGood, container)
                    } else {
                        MarkScreenStatus.INCORRECT_EAN_FORMAT
                    }
                } else {
                    MarkScreenStatus.NO_MARKTYPE_IN_SETTINGS
                }
        }
    }

    private suspend fun setOpenGood(
            result: GoodInfoResult,
            container: Pair<String, Mark.Container>? = null,
            mrc: String = ""
    ): MarkScreenStatus {
        val taskFromManager = openManager.currentTask
        val goodFromManager = openManager.currentGood

        return with(result) {
            taskFromManager.value?.let { task ->
                goodFromManager.value?.let { good ->
                    val goodEan = eanInfo?.ean.orEmpty()
                    val markType = getMarkType()

                    val createdGood = Good(
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
                            volume = materialInfo?.volume?.toDoubleOrNull() ?: ZERO_VOLUME,
                            markType = markType,
                            markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                            maxRetailPrice = mrc,
                            type = materialInfo?.goodType.orEmpty(),
                            purchaseGroup = materialInfo?.purchaseGroup.orEmpty()
                    )
                    if(createdGood.material != good.material) {
                        createdGoodToShowError.value = createdGood
                        MarkScreenStatus.NOT_SAME_GOOD
                    }

                    //Проверим есть ли такая маркировка в справочнике
                    if (database.isMarkTypeInDatabase(createdGood.markType)) {
                        //Проверим совпадают ли у товара в карточке и найденого товара маркировки
                        if (createdGood.markType == good.markType) {
                            //Проверим если товар табак и если его мрц нет в списке мрц задания
                            if (good.isTobacco() && task.isMrcNotInTaskMrcList(mrc)) {
                                createdGoodToShowError.value = createdGood
                                MarkScreenStatus.MRC_NOT_SAME
                            } else {
                                setFoundGood(createdGood, container)
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


    /**
     * Форматирует мрц, если он есть
     * Он приходит в копейках и общий для всех товаров в блоке/коробке.
     * Умрез - количество в блоке/коробке, приходит из ФМ. Делим на него и делим на 100 чтобы в рублях получить
     * */
    private suspend fun String.getFormattedMrc(ean: String): String {
        return this.takeIf { it.isNotEmpty() }?.run {
            val wholeMrc = this.toDoubleOrNull()
            wholeMrc?.let {
                val umrez = database.getEanInfo(ean)?.umrez?.toDouble() ?: DEFAULT_UMREZ
                val partedMrc = wholeMrc.div(umrez)
                val partedMrcInRub = partedMrc.div(100).dropZeros()
                partedMrcInRub
            }.orEmpty()
        }.orEmpty()
    }


    /**
     * Если в метод передали контейнер, то это значит что отсканирована марка, и надо выполнять дальнейшие проверки
     * Если нет, то значит отсканирован штрихкод товара и его надо показать
     * */
    private suspend fun setFoundGood(
            foundGood: Good,
            container: Pair<String, Mark.Container>? = null
    ): MarkScreenStatus {
        val manager = chooseManager()
        val currentGood = manager.currentGood.value
        return if(currentGood != null && foundGood.material != currentGood.material) {
            createdGoodToShowError.value = foundGood
            MarkScreenStatus.NOT_SAME_GOOD
        } else {
             if (container != null) {
                val params = getMarkParams(foundGood, container)
                checkMarkNetRequest(params, foundGood)
            } else {
                manager.updateCurrentGood(foundGood)
                MarkScreenStatus.OK_BUT_NEED_TO_SCAN_MARK
            }
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
    private fun handleCheckMarkNetRequestResult(
            result: MarkCartonBoxGoodInfoNetRequestResult,
            foundGood: Good
    ): MarkScreenStatus {
        val status = result.getMarkStatus()
        val marks = result.marks
        val properties = result.properties
        return marks?.let { resultMarks ->
            tempMarks.let { localTempMarks ->
                val mappedMarks = resultMarks.mapToMarkList(foundGood)
                val currentGood = chooseGood()
                when (status) {
                    MarkStatus.GOOD_CARTON -> {
                        if (currentGood?.isTobaccoAndFoundGoodHasDifferentMrc(foundGood) == true) {
                            Logg.e { "foundGood: ${foundGood.maxRetailPrice} currentGood: ${currentGood.maxRetailPrice}" }
                            this.mappedMarks = mappedMarks
                            tempGood.value = foundGood
                            MarkScreenStatus.MRC_NOT_SAME_IN_BASKET
                        } else {
                            addOrDeleteMarksFromTemp(
                                    restProperties = properties,
                                    localTempMarks = localTempMarks,
                                    mappedMarks = mappedMarks,
                                    foundGood = foundGood,
                                    screenStatusIfAlreadyScanned = MarkScreenStatus.CARTON_ALREADY_SCANNED
                            )
                        }
                    }
                    MarkStatus.GOOD_MARK -> {
                        addOrDeleteMarksFromTemp(
                                restProperties = properties,
                                localTempMarks = localTempMarks,
                                mappedMarks = mappedMarks,
                                foundGood = foundGood,
                                screenStatusIfAlreadyScanned = MarkScreenStatus.MARK_ALREADY_SCANNED
                        )
                    }
                    MarkStatus.GOOD_BOX -> {
                        addOrDeleteMarksFromTemp(
                                restProperties = properties,
                                localTempMarks = localTempMarks,
                                mappedMarks = mappedMarks,
                                foundGood = foundGood,
                                screenStatusIfAlreadyScanned = MarkScreenStatus.BOX_ALREADY_SCANNED
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
            foundGood: Good,
            screenStatusIfAlreadyScanned: MarkScreenStatus
    ): MarkScreenStatus {
        Logg.e { "foundGood: $foundGood mappedMarks: $mappedMarks" }
        return if (localTempMarks.isAnyAlreadyIn(mappedMarks) || foundGood.marks.isAnyAlreadyIn(mappedMarks)) {
            this.mappedMarks = mappedMarks
            screenStatusIfAlreadyScanned
        } else {
            val manager = chooseManager()
            manager.updateCurrentGood(foundGood)
            val restPropertiesMapped = restProperties?.map { propertyFromRest ->
                GoodProperty(
                        gtin = propertyFromRest.ean.orEmpty(),
                        property = propertyFromRest.propertyName.orEmpty(),
                        value = propertyFromRest.propertyValue.orEmpty()
                )
            }.orEmpty()
            properties = properties.union(restPropertiesMapped).toMutableList()

            localTempMarks.addAll(mappedMarks)

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

    override suspend fun handleYesDeleteMappedMarksFromTempCallBack() {
        tempMarks.removeAll(mappedMarks)
        val manager = chooseManager()
        manager.removeMarksFromGoods(mappedMarks)
    }

    override fun handleYesSaveAndOpenAnotherBox() {
        tempMarks.clear()
        tempMarks.addAll(mappedMarks)
        val manager = chooseManager()
        manager.updateCurrentGood(tempGood.value)
    }

    private fun chooseManager(): ITaskManager<*> {
        return when (workType) {
            WorkType.CREATE -> createManager
            WorkType.OPEN -> openManager
        }
    }

    private fun chooseGood(): Good? {
        return chooseManager().currentGood.value
    }

    override fun getMarkFailure(): Failure {
        return failure
    }

    companion object {
        private const val DEFAULT_UMREZ = 1.0
        private const val DEFAULT_INNER_QUALITY_VALUE = 1.0
    }
}