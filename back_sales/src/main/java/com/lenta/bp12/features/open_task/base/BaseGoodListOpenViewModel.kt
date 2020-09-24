package com.lenta.bp12.features.open_task.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.base.BaseGoodListViewModel
import com.lenta.bp12.features.open_task.base.interfaces.IBaseGoodListOpenViewModel
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.platform.ZERO_VOLUME
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Базовый класс вьюмодели списка товаров для Работы с заданиями
 * дети:
 * @see com.lenta.bp12.features.open_task.good_list.GoodListViewModel
 * @see com.lenta.bp12.features.basket.basket_good_list.BasketOpenGoodListViewModel
 * */
abstract class BaseGoodListOpenViewModel: BaseGoodListViewModel<TaskOpen, IOpenTaskManager>(), IBaseGoodListOpenViewModel {

    override val task by unsafeLazy {
        manager.currentTask
    }

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    /**
     * Метод проверяет длину отсканированного/введенного кода
     * */
    override fun checkSearchNumber(number: String) {
        manager.ean = number
        actionByNumber(
                number = number,
                funcForEan = {
                    getGoodByEan(number)
                },
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
            foundGood?.let(::setFoundGood).orIfNull {
                actionWhenGoodNotFoundByEan(ean)
            }
        }
    }

    private suspend fun actionWhenGoodNotFoundByEan(ean: String) {
        if (task.value?.isStrict == false) {
            loadGoodInfoByEan(ean)
        } else {
            navigator.showGoodIsMissingInTask()
        }
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
        }.either(
                fnL = ::handleFailure,
                fnR = ::handleLoadGoodInfoResult
        )
    }

    override fun setFoundGood(foundGood: Good) {
        with(navigator) {
            manager.updateCurrentGood(foundGood)
            if (foundGood.isMarked()) {
                openMarkedGoodInfoOpenScreen()
                checkThatNoneOfGoodAreMarkType(foundGood.getNameWithMaterial())
            } else {
                openGoodInfoOpenScreen()
            }
        }
    }

    /**
     * Метод ищет есть ли уже товар в задании по Sap коду,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByMaterial(material: String) {
        launchUITryCatch {
            manager.clearEan()
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByMaterial(material) }
            navigator.hideProgress()
            foundGood?.let(::setFoundGood).orIfNull {
                actionWhenGoodNotFoundByMaterial(material)
            }
        }
    }

    private suspend fun actionWhenGoodNotFoundByMaterial(material: String) {
        if (task.value?.isStrict == false) {
            loadGoodInfoByMaterial(material)
        } else {
            navigator.showGoodIsMissingInTask()
        }
    }

    private suspend fun loadGoodInfoByMaterial(material: String) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(
                GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        material = material,
                        taskType = task.value?.type?.code.orEmpty(),
                        mode = ScanInfoMode.MARK.mode.toString()
                )
        ).also {
            navigator.hideProgress()
        }.either(
                fnL = ::handleFailure,
                fnR = ::handleLoadGoodInfoResult
        )
    }

    private fun checkMark(number: String) {
        launchUITryCatch {
            with(navigator) {
                manager.clearEan()
                showProgressLoadingData()
                val screenStatus = markManager.checkMark(number, WorkType.OPEN, false)
                hideProgress()
                when (screenStatus) {
                    MarkScreenStatus.OK -> openMarkedGoodInfoOpenScreen()
                    MarkScreenStatus.CANT_SCAN_PACK -> showCantScanPackAlert()
                    MarkScreenStatus.NO_MARKTYPE_IN_SETTINGS -> showNoMarkTypeInSettings()
                    MarkScreenStatus.INCORRECT_EAN_FORMAT -> showIncorrectEanFormat()
                    else -> Unit
                }
            }
        }
    }

    private fun handleLoadGoodInfoResult(result: GoodInfoResult) {
        launchUITryCatch {
            val isGoodCorrespondToTask = manager.isGoodCorrespondToTask(result)
            val isGoodCanBeAdded = manager.isGoodCanBeAdded(result)
            val isWholesaleTask = manager.isWholesaleTaskType
            val goodKind = result.getGoodKind()
            val isGoodVet = goodKind == GoodKind.VET
            val isGoodExcise = goodKind == GoodKind.EXCISE

            with(navigator) {
                when {
                    isWholesaleTask && isGoodVet -> showCantAddVetToWholeSale()
                    isWholesaleTask && isGoodExcise -> showCantAddExciseGoodForWholesale()
                    isGoodCorrespondToTask && isGoodCanBeAdded -> setGood(result)
                    isGoodCorrespondToTask -> showGoodCannotBeAdded()
                    else -> showNotMatchTaskSettingsAddingNotPossible()
                }
            }

        }
    }

    private suspend fun setGood(result: GoodInfoResult) {
        with(result) {
            val markType = getMarkType()
            val goodOpen = Good(
                    ean = eanInfo?.ean.orEmpty(),
                    material = materialInfo?.material.orEmpty(),
                    name = materialInfo?.name.orEmpty(),
                    section = materialInfo?.section.orEmpty(),
                    matrix = getMatrixType(materialInfo?.matrix.orEmpty()),
                    kind = getGoodKind(),
                    control = getControlType(),
                    commonUnits = database.getUnitsByCode(materialInfo?.commonUnitsCode.orEmpty()),
                    innerUnits = database.getUnitsByCode(materialInfo?.innerUnitsCode.orEmpty()),
                    innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull()
                            ?: ZERO_QUANTITY,
                    provider = task.value?.takeIf { manager.isWholesaleTaskType.not() }
                            ?.provider
                            ?: ProviderInfo.getEmptyProvider(),
                    producers = producers.orEmpty().toMutableList(),
                    volume = materialInfo?.volume?.toDoubleOrNull() ?: ZERO_VOLUME,
                    markType = markType,
                    markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                    type = materialInfo?.goodType.orEmpty(),
                    purchaseGroup = materialInfo?.purchaseGroup.orEmpty()
            )

            setFoundGood(goodOpen)
        }
    }
}