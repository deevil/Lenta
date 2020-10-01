package com.lenta.bp12.features.create_task.base

import com.lenta.bp12.features.base.BaseGoodListViewModel
import com.lenta.bp12.features.create_task.base.interfaces.IBaseGoodListCreateViewModel
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.MarkScreenStatus
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.model.actionByNumber
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.platform.ZERO_VOLUME
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Базовый класс вьюмодели ответственный за список товаров
 * Дети:
 * @see com.lenta.bp12.features.create_task.task_content.TaskContentViewModel
 * @see com.lenta.bp12.features.basket.basket_good_list.BasketCreateGoodListViewModel
 * */
abstract class BaseGoodListCreateViewModel : BaseGoodListViewModel<TaskCreate, ICreateTaskManager>(),
        IBaseGoodListCreateViewModel, OnOkInSoftKeyboardListener {

    /**
     * Метод проверяет длину отсканированного/введенного кода
     * */
    override fun checkSearchNumber(number: String) {
        manager.ean = number
        actionByNumber(
                number = number,
                funcForEan = ::getGoodByEan,
                funcForMaterial = ::getGoodByMaterial,
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForMark = ::checkMark,
                funcForNotValidBarFormat = navigator::showIncorrectEanFormat
        )
        numberField.value = ""
    }

    /**
     * Метод ищет есть ли уже товар в задании по EAN,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    override fun getGoodByEan(ean: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByEan(ean) }
            navigator.hideProgress()
            foundGood?.let(::setFoundGood).orIfNull { loadGoodInfoByEan(ean) }
        }
    }

    /**
     * Метод ищет есть ли уже товар в задании по Sap коду,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    override fun getGoodByMaterial(material: String) {
        launchUITryCatch {
            manager.clearEan()
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByMaterial(material) }
            navigator.hideProgress()
            foundGood?.let(::setFoundGood).orIfNull { loadGoodInfoByMaterial(material) }
        }
    }

    override fun checkMark(number: String) {
        launchUITryCatch {
            with(navigator) {
                manager.clearEan()
                showProgressLoadingData()
                val screenStatus = markManager.checkMark(number, WorkType.CREATE, false)
                Logg.d { "--> MarkScreenStatus: ${screenStatus.name}" }
                hideProgress()
                when (screenStatus) {
                    MarkScreenStatus.OK -> openMarkedGoodInfoCreateScreen()
                    MarkScreenStatus.CANT_SCAN_PACK -> showCantScanPackAlert()
                    MarkScreenStatus.NO_MARKTYPE_IN_SETTINGS -> navigator.showNoMarkTypeInSettings()
                    MarkScreenStatus.INCORRECT_EAN_FORMAT -> navigator.showIncorrectEanFormat()
                    else -> Unit
                }
            }
        }
    }

    override fun setFoundGood(foundGood: Good) {
        with(navigator) {
            manager.updateCurrentGood(foundGood)
            if (foundGood.isMarked()) {
                openMarkedGoodInfoCreateScreen()
                checkThatNoneOfGoodAreMarkType(foundGood.getNameWithMaterial())
            } else {
                manager.updateCurrentGood(foundGood)
                openGoodInfoCreateScreen()
            }
        }
    }

    override suspend fun loadGoodInfoByEan(ean: String) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(
                GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        ean = ean,
                        taskType = task.value?.type?.code.orEmpty()
                )
        ).also {
            navigator.hideProgress()
        }.either(::handleFailure) {
            handleLoadGoodInfoResult(it)
        }
    }

    override suspend fun loadGoodInfoByMaterial(material: String) {
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
            handleLoadGoodInfoResult(result)
        }
    }

    override fun handleLoadGoodInfoResult(result: GoodInfoResult) {
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
                    isGoodCanBeAdded -> setGood(result)
                    else -> showGoodCannotBeAdded()
                }
            }
        }
    }

    /**
     * Метод проверяет маркированный товар пришел или нет.
     * если маркированный, то показываем сообщение о том что нужно сканировать марку,
     * если нет, то создаём его и показываем карточку
     */
    override fun setGood(result: GoodInfoResult) {
        launchUITryCatch {
            with(result) {
                val goodEan = eanInfo?.ean.orEmpty()
                val markType = getMarkType()

                val good = Good(
                        ean = goodEan,
                        eans = database.getEanMapByMaterialUnits(
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
                        providers = providers.takeIf { manager.isWholesaleTaskType.not() }
                                .orEmpty()
                                .toMutableList(),
                        producers = producers.orEmpty().toMutableList(),
                        volume = materialInfo?.volume?.toDoubleOrNull() ?: ZERO_VOLUME,
                        markType = markType,
                        markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                        purchaseGroup = materialInfo?.purchaseGroup.orEmpty()
                )

                setFoundGood(good)
            }
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkSearchNumber(numberField.value.orEmpty())
        return true
    }

}