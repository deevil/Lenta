package com.lenta.bp12.features.open_task.base

import com.lenta.bp12.features.base.BaseGoodListViewModel
import com.lenta.bp12.features.open_task.base.interfaces.IBaseGoodListOpenViewModel
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.MarkScreenStatus
import com.lenta.bp12.model.ScanInfoMode
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.platform.ZERO_VOLUME
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.request.pojo.MaterialInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.utilities.extentions.launchUITryCatch

/**
 * Базовый класс вьюмодели списка товаров для Работы с заданиями
 * дети:
 * @see com.lenta.bp12.features.open_task.good_list.GoodListViewModel
 * @see com.lenta.bp12.features.basket.basket_good_list.BasketOpenGoodListViewModel
 * */
abstract class BaseGoodListOpenViewModel: BaseGoodListViewModel<TaskOpen, IOpenTaskManager>(), IBaseGoodListOpenViewModel {

    override suspend fun actionWhenGoodNotFoundByEan(ean: String) {
        if (task.value?.isStrict == false) {
            loadGoodInfoByEan(ean)
        } else {
            navigator.showGoodIsMissingInTask()
        }
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

    override suspend fun actionWhenGoodNotFoundByMaterial(material: String) {
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

    override fun checkMark(number: String) {
        launchUITryCatch {
            manager.clearEan()
            navigator.showProgressLoadingData()
            val screenStatus = markManager.checkMark(number, WorkType.OPEN, false)
            navigator.hideProgress()
            processScreenStatusFromMark(screenStatus)
        }
    }

    private fun processScreenStatusFromMark(screenStatus: MarkScreenStatus) {
        with(navigator) {
            when (screenStatus) {
                MarkScreenStatus.OK -> openMarkedGoodInfoOpenScreen()
                MarkScreenStatus.CANT_SCAN_PACK -> showCantScanPackAlert()
                MarkScreenStatus.NO_MARKTYPE_IN_SETTINGS -> showNoMarkTypeInSettings()
                MarkScreenStatus.INCORRECT_EAN_FORMAT -> showIncorrectEanFormat()
                else -> Unit
            }
        }
    }

    override fun handleLoadGoodInfoResult(result: GoodInfoResult) {
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
                    innerQuantity = materialInfo.getGoodInnerQuantity(),
                    provider = getGoodProvider(),
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

    private fun MaterialInfo?.getGoodInnerQuantity(): Double {
        return this?.innerQuantity?.toDoubleOrNull()
                ?: ZERO_QUANTITY
    }

    private fun getGoodProvider(): ProviderInfo {
        return task.value?.takeIf { manager.isWholesaleTaskType.not() }
                ?.provider
                ?: ProviderInfo.getEmptyProvider()
    }
}