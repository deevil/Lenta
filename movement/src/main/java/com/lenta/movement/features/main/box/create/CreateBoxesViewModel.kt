package com.lenta.movement.features.main.box.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.models.ExciseBox
import com.lenta.movement.models.ExciseStamp
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.repositories.IBoxesRepository
import com.lenta.movement.platform.Constants.EMPTY
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.*
import com.lenta.movement.requests.network.models.checkExciseBox.CheckExciseBoxParams
import com.lenta.movement.requests.network.models.scanInfoNetRequest.ScanInfoParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.constants.Constants.EXCISE_BOX_26
import com.lenta.shared.platform.constants.Constants.EXCISE_MARK_150
import com.lenta.shared.platform.constants.Constants.EXCISE_MARK_68
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class CreateBoxesViewModel : CoreViewModel(),
        PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var exciseStampNetRequest: ExciseStampNetRequest

    @Inject
    lateinit var checkExciseBoxNetRequest: CheckExciseBoxNetRequest

    @Inject
    lateinit var scanInfoNetRequest: ScanInfoNetRequest

    @Inject
    lateinit var boxesRepository: IBoxesRepository

    private val haveUnsavedData: Boolean
        get() = stamps.value.orEmpty().isNotEmpty() || boxNumber.value.orEmpty().isNotEmpty()

    val selectionsHelper = SelectionItemsHelper()

    val selectedPagePosition = MutableLiveData(0)
    val selectedPage = MutableLiveData(CreateBoxesPage.FILLING)

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val stamps: MutableLiveData<List<ExciseStamp>> = MutableLiveData(emptyList())
    val stampsQuantity by unsafeLazy {
        stamps.map { stampsList ->
            liveData {
                val size = stampsList?.size ?: 0
                emit(size)
            }
        }
    }
    val boxNumber: MutableLiveData<String> = MutableLiveData()

    val boxList: MutableLiveData<List<BoxListItem>> = MutableLiveData()

    val rollbackEnabled: MutableLiveData<Boolean> = stamps.map { stamps ->
        stamps.orEmpty().isNotEmpty()
    }

    val addAndApplyEnabled: MutableLiveData<Boolean> = combineLatest(productInfo, stamps, boxNumber)
            .map {
                val productInfoOrNull = it?.first
                val stampsOrEmpty = it?.second.orEmpty()
                val boxNumberOrNull = it?.third

                stampsOrEmpty.size == productInfoOrNull?.quantityInvestments && boxNumberOrNull != null
            }

    val deleteEnabled: MutableLiveData<Boolean> =
            selectionsHelper.selectedPositions.map { selectedPositions ->
                selectedPositions.orEmpty().isNotEmpty()
            }

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
        selectedPage.value = CreateBoxesPage.values().getOrNull(position)
    }

    fun onResume() = updateBoxes()

    fun getTitle(): String {
        return buildString {
            productInfo.value?.let { product ->
                val shortMaterialNumber = product.materialNumber.takeLast(6)
                val description = product.description
                append(shortMaterialNumber)
                append(" ")
                append(description)
            }
        }
    }

    fun onScanResult(data: String) {
        when (data.length) {
            EXCISE_MARK_68, EXCISE_MARK_150 -> scanStamp(data)
            EXCISE_BOX_26 -> scanBox(data)
            else -> scanGoods(data)
        }
    }

    fun onRollbackClick() {
        val stampList = stamps.value.orEmpty()
        if (stampList.isNotEmpty()) {
            val stampListWithoutLastOne = stampList.dropLast(1)
            stamps.value = stampListWithoutLastOne
        }
    }

    fun onAddClick() {
        saveAndClearFields()
    }

    fun onCompleteClick() {
        screenNavigator.goBack()
        saveAndClearFields()
    }

    fun onBackPressed() {
        if (haveUnsavedData) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        screenNavigator.goBack()
                        screenNavigator.goBack()
                    })
        } else screenNavigator.goBack()
    }

    fun onDeleteClick() {
        selectionsHelper.selectedPositions.value.orEmpty()
                .forEach { doRemovePosition ->
                    productInfo.value?.let { productInfoValue ->
                        val boxes = boxesRepository.getBoxesByProduct(productInfoValue)
                        boxes.getOrNull(doRemovePosition)?.let (boxesRepository::removeBox)
                    }
                }
        updateBoxes()
        selectionsHelper.clearPositions()
    }

    private fun deleteStamp(stamp: ExciseStamp) {
        stamps.value?.let { stampsValue ->
            val newStampsList = stampsValue.toMutableList()
            newStampsList.remove(stamp)
            stamps.value = newStampsList
        }
    }

    private fun findBoxByStampCode(stampCode: String): ExciseBox? {
        val productInfoValue = productInfo.value
        return productInfoValue?.let {
             boxesRepository.getBoxesByProduct(it).find { box ->
                box.stamps.any { stamp ->
                    stamp.code == stampCode
                }
            }
        }
    }


    private fun scanStamp(stampCode: String) {
        val stampList = stamps.value
        stampList?.let { stampListValue ->
            productInfo.value?.let { productInfo ->

                stampListValue.find { it.code == stampCode }?.let { stamp ->
                    screenNavigator.openStampWasAddedDialog(
                            yesCallbackFunc = { deleteStamp(stamp) }
                    )
                    return
                }

                findBoxByStampCode(stampCode)?.let {
                    screenNavigator.openStampWasAddedDialogInAnotherBox(it)
                    return
                }

                if (productInfo.quantityInvestments == stampListValue.size) {
                    screenNavigator.openStampMaxCountDialog()
                    return
                }

                launchUITryCatch {
                    screenNavigator.showProgress(LOADING_STAMP_INFO)
                    exciseStampNetRequest(
                            params = ExciseStampParams(
                                    tk = sessionInfo.market.orEmpty(),
                                    materialNumber = productInfo.materialNumber,
                                    stampCode = stampCode
                            )
                    ).either(
                            fnL = { failure ->
                                screenNavigator.openAlertScreen(failure)
                            },
                            fnR = ::onScanStampSuccess
                    )
                    screenNavigator.hideProgress()
                }
            }
        }
    }

    private fun onScanStampSuccess(exciseStamp: ExciseStamp) {
        val stampListValue = stamps.value
        stampListValue?.let {
            if (stampListValue.isNotEmpty() &&
                    stampListValue.firstOrNull()?.manufacturerName == exciseStamp.manufacturerName
            ) {
                stamps.value = stampListValue + exciseStamp
            } else {
                stamps.value = listOf(exciseStamp)
            }
        }
    }

    private fun scanBox(boxCode: String) {
        productInfo.value?.let { productInfoValue ->
            if (boxesRepository.getBoxesByProduct(productInfoValue).any { it.code == boxCode }) {
                screenNavigator.openBoxNumberWasUsedDialog()
                return
            }
        }
        launchUITryCatch {
            screenNavigator.showProgress(checkExciseBoxNetRequest)
            checkExciseBoxNetRequest(
                    params = CheckExciseBoxParams(
                            tk = sessionInfo.market.orEmpty(),
                            materialNumber = productInfo.value?.materialNumber.orEmpty(),
                            boxCode = boxCode
                    )
            ).either(
                    fnL = { failure ->
                        screenNavigator.openAlertScreen(failure)
                    },
                    fnR = { checkExciseBoxStatus ->
                        onScanBoxSuccess(boxCode, checkExciseBoxStatus)
                    }
            )
            screenNavigator.hideProgress()
        }
    }

    private fun onScanBoxSuccess(boxCode: String, checkExciseBoxStatus: CheckExciseBoxStatus) {
        when (checkExciseBoxStatus) {
            CheckExciseBoxStatus.Correct -> {
                boxNumber.postValue(boxCode)
            }
            is CheckExciseBoxStatus.BoxFound -> {
                screenNavigator.openBoxRewriteDialog(
                        checkExciseBoxStatus.msg,
                        yesCallbackFunc = {
                            boxNumber.postValue(boxCode)
                        })
            }
            is CheckExciseBoxStatus.Other -> {
                screenNavigator.openAlertScreen(InfoFailure(checkExciseBoxStatus.msg))
            }
        }
    }

    private fun scanGoods(code: String, fromScan: Boolean = true) {
        if (addAndApplyEnabled.value == false) return

        launchUITryCatch {
            screenNavigator.showProgress(scanInfoNetRequest)
            val scanCodeInfo = ScanCodeInfo(code, if (fromScan) null else 0.0)
            val request = scanInfoNetRequest(
                    params = ScanInfoParams(
                            ean = scanCodeInfo.eanNumberForSearch.orEmpty(),
                            tk = sessionInfo.market.orEmpty(),
                            matNr = scanCodeInfo.materialNumberForSearch.orEmpty(),
                            codeEBP = CODE_EBP
                    )
            )
            request.either(
                    fnL = { failure ->
                        screenNavigator.openAlertScreen(failure)
                    },
                    fnR = ::onScanGoodsSuccess
            )
            screenNavigator.hideProgress()
        }
    }


    private fun onScanGoodsSuccess(serverProductInfo: ProductInfo) {
        val serverProductInfoMaterialNumber = serverProductInfo.materialNumber
        val productInfoValue = productInfo.value
        val serverProductInfoType = serverProductInfo.type
        productInfoValue?.let {
            val productInfoMaterialNumber = it.materialNumber
            if (productInfoMaterialNumber == serverProductInfoMaterialNumber) {
                return
            }
        }

        if (serverProductInfoType != ProductType.ExciseAlcohol) {
            screenNavigator.openProductIncorrectForCreateBox(serverProductInfo)
        } else {
            screenNavigator.goBack()
            screenNavigator.openCreateBoxByProduct(serverProductInfo)
            saveAndClearFields()
        }
    }

    private fun saveAndClearFields() {
        productInfo.value?.let { productInfoValue ->
            val newBox = ExciseBox(
                    code = boxNumber.value.orEmpty(),
                    stamps = stamps.value.orEmpty(),
                    productInfo = productInfoValue
            )

            boxesRepository.addBoxes(newBox)
            stamps.value = emptyList()
            boxNumber.value = EMPTY
            updateBoxes()

            screenNavigator.openBoxSavedDialog(newBox)
        }.orIfNull {
            Logg.e { "productInfo empty" }
            screenNavigator.openAlertScreen(Failure.SapError("Ошибка значения товара"))
        }
    }

    private fun updateBoxes() {
        productInfo.value?.let { productInfoValue ->
            val boxes = boxesRepository.getBoxesByProduct(productInfoValue).mapIndexed { index, box ->
                val title = buildString {
                    val boxCodeFirstFive = box.code.take(5)
                    val boxCodeLastFive = box.code.takeLast(5)
                    val dateOfPour = box.stamps.firstOrNull()?.dateOfPour.orEmpty()
                    when (box.code.length) {
                        in 0..10 -> append(box.code)
                        else -> {
                            append(boxCodeFirstFive)
                            append("...")
                            append(boxCodeLastFive)
                        }
                    }
                    append(" // ")
                    append(dateOfPour)
                }
                BoxListItem(
                        number = (index + 1).toString(),
                        title = title,
                        subtitle = box.stamps.firstOrNull()?.manufacturerName.orEmpty()
                )
            }
            boxList.value = boxes
        }
    }

    companion object {
        private const val CODE_EBP = "MVM"
        private const val LOADING_STAMP_INFO = "Загрузка информации о марке"
    }


}