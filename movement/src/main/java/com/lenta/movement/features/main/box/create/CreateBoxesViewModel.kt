package com.lenta.movement.features.main.box.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.models.ExciseBox
import com.lenta.movement.models.ExciseStamp
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.repositories.IBoxesRepository
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.*
import com.lenta.movement.requests.network.models.checkExciseBox.CheckExciseBoxParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.EgaisStampVersion
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import kotlinx.coroutines.launch
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
                val size = stampsList?.size ?: "0"
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
        selectedPage.value = CreateBoxesPage.values()[position]
    }

    fun onResume() {
        updateBoxes()
    }

    fun getTitle(): String {
        return productInfo.value?.let {
            "${it.materialNumber.takeLast(6)} ${it.description}"
        }.orEmpty()
    }

    fun onScanResult(data: String) {
        when (data.length) {
            EgaisStampVersion.V2.version,
            EgaisStampVersion.V3.version -> scanStamp(data)
            SCAN_LENGTH_BOX -> scanBox(data)
            else -> scanGoods(data)
        }
    }

    fun onRollbackClick() {
        val stampList = stamps.value.orEmpty()
        if (stampList.isNotEmpty()) {
            stamps.postValue(stampList.dropLast(1))
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
            screenNavigator.openUnsavedDataDialog(yesCallbackFunc = {
                screenNavigator.goBack()
                screenNavigator.goBack()
            })
        } else {
            screenNavigator.goBack()
        }
    }

    fun onDeleteClick() {
        selectionsHelper.selectedPositions.value.orEmpty()
                .forEach { doRemovePosition ->
                    productInfo.value?.let { productInfoValue ->
                        val removeBoxTarget = boxesRepository.getBoxesByProduct(productInfoValue)[doRemovePosition]
                        boxesRepository.removeBox(removeBoxTarget)
                    }
                }
        updateBoxes()

        selectionsHelper.clearPositions()
    }

    private fun scanStamp(stampCode: String) {

        if (stamps.value.orEmpty().any { it.code == stampCode }) {
            screenNavigator.openStampWasAddedDialog()
            return
        }

        if (productInfo.value?.quantityInvestments == stamps.value?.size) {
            screenNavigator.openStampMaxCountDialog()
            return
        }

        productInfo.value?.let { productInfoValue ->
            boxesRepository.getBoxesByProduct(productInfoValue)
                    .find { box ->
                        box.stamps.any { stamp ->
                            stamp.code == stampCode
                        }
                    }
                    ?.let { findBoxWithStamp ->
                        screenNavigator.openStampWasAddedDialog(findBoxWithStamp)
                        return
                    }
        }


        viewModelScope.launch {
            screenNavigator.showProgress(LOADING_STAMP_INFO)
            exciseStampNetRequest(
                    params = ExciseStampParams(
                            tk = sessionInfo.market.orEmpty(),
                            materialNumber = productInfo.value?.materialNumber.orEmpty(),
                            stampCode = stampCode
                    )
            ).either(
                    fnL = { failure ->
                        screenNavigator.openAlertScreen(failure)
                    },
                    fnR = { exciseStamp ->
                        val stampList = stamps.value.orEmpty()

                        if (stampList.isNotEmpty()
                                && stampList.first().manufacturerName == exciseStamp.manufacturerName
                        ) {
                            stamps.postValue(stampList + exciseStamp)
                        } else {
                            stamps.postValue(listOf(exciseStamp))
                        }
                    }
            )
            screenNavigator.hideProgress()
        }
    }

    private fun scanBox(boxCode: String) {
        productInfo.value?.let { productInfoValue ->
            if (boxesRepository.getBoxesByProduct(productInfoValue).any { it.code == boxCode }) {
                screenNavigator.openBoxNumberWasUsedDialog()
                return
            }
        }

        viewModelScope.launch {
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
            )
            screenNavigator.hideProgress()
        }
    }

    private fun scanGoods(code: String, fromScan: Boolean = true) {
        if (addAndApplyEnabled.value == false) return

        viewModelScope.launch {
            screenNavigator.showProgress(scanInfoNetRequest)
            val scanCodeInfo = ScanCodeInfo(code, if (fromScan) null else 0.0)
            scanInfoNetRequest(
                    params = ScanInfoParams(
                            ean = scanCodeInfo.eanNumberForSearch.orEmpty(),
                            tk = sessionInfo.market.orEmpty(),
                            matNr = scanCodeInfo.materialNumberForSearch.orEmpty(),
                            codeEBP = CODE_EBP
                    )
            ).either(
                    fnL = { failure ->
                        screenNavigator.openAlertScreen(failure)
                    },
                    fnR = { serverProductInfo ->
                        productInfo.value?.let { productInfoValue ->
                            if (productInfoValue.materialNumber == serverProductInfo.materialNumber) {
                                return@either Unit
                            }
                        }

                        if (serverProductInfo.type != ProductType.ExciseAlcohol) {
                            screenNavigator.openProductIncorrectForCreateBox(serverProductInfo)
                        } else {
                            screenNavigator.goBack()
                            screenNavigator.openCreateBoxByProduct(serverProductInfo)
                            saveAndClearFields()
                        }
                    }
            )
            screenNavigator.hideProgress()
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
            stamps.postValue(emptyList())
            boxNumber.postValue("")
            updateBoxes()

            screenNavigator.openBoxSavedDialog(newBox)
        } ?: Logg.e {
            "productInfo empty"
        }
    }

    private fun updateBoxes() {
        productInfo.value?.let { productInfoValue ->
            val boxes = boxesRepository.getBoxesByProduct(productInfoValue).mapIndexed { index, box ->
                BoxListItem(
                        number = (index + 1).toString(),
                        title = "${box.code.take(5)}...${box.code.takeLast(5)} // ${box.stamps.first().dateOfPour}",
                        subtitle = box.stamps.first().manufacturerName
                )
            }

            boxList.postValue(boxes)
        }
    }

    companion object {
        private const val SCAN_LENGTH_BOX = 26
        private const val CODE_EBP = "MVM"
        private const val LOADING_STAMP_INFO = "Загрузка информации о марке"
    }
}