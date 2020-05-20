package com.lenta.movement.features.main.box.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.models.ExciseBox
import com.lenta.movement.models.ExciseStamp
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.repositories.IBoxesRepository
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
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
            68, 150 -> scanStamp(data)
            26 -> scanBox(data)
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
                val removeBoxTarget = boxesRepository.getBoxesByProduct(productInfo.value!!)[doRemovePosition]

                boxesRepository.removeBox(removeBoxTarget)
            }
        updateBoxes()

        selectionsHelper.clearPositions()
    }

    private fun scanStamp(stampCode: String) {
        if (productInfo.value?.quantityInvestments == stamps.value?.size) {
            screenNavigator.openStampMaxCountDialog()
            return
        }

        if (stamps.value.orEmpty().any { it.code == stampCode }) {
            screenNavigator.openStampWasAddedDialog()
            return
        }

        boxesRepository.getBoxesByProduct(productInfo.value!!)
            .find { box ->
                box.stamps.any { stamp ->
                    stamp.code == stampCode
                }
            }
            ?.let { findBoxWithStamp ->
                screenNavigator.openStampWasAddedDialog(findBoxWithStamp)
                return
            }

        viewModelScope.launch {
            screenNavigator.showProgress(getTitle())
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
        if (boxesRepository.getBoxesByProduct(productInfo.value!!).any { it.code == boxCode }) {
            screenNavigator.openBoxNumberWasUsedDialog()
            return
        }

        viewModelScope.launch {
            screenNavigator.showProgress(getTitle())
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
                    codeEBP = "MVM"
                )
            ).either(
                fnL = { failure ->
                    screenNavigator.openAlertScreen(failure)
                },
                fnR = { productInfo ->
                    if (this@CreateBoxesViewModel.productInfo.value!!.materialNumber == productInfo.materialNumber) {
                        return@either Unit
                    }

                    if (productInfo.type != ProductType.ExciseAlcohol) {
                        screenNavigator.openProductIncorrectForCreateBox(productInfo)
                    } else {
                        screenNavigator.goBack()
                        screenNavigator.openCreateBoxByProduct(productInfo)
                        saveAndClearFields()
                    }
                }
            )
            screenNavigator.hideProgress()
        }
    }

    private fun saveAndClearFields() {
        val newBox = ExciseBox(
            code = boxNumber.value.orEmpty(),
            stamps = stamps.value.orEmpty(),
            productInfo = productInfo.value!!
        )

        boxesRepository.addBoxes(newBox)
        stamps.postValue(emptyList())
        boxNumber.postValue("")
        updateBoxes()

        screenNavigator.openBoxSavedDialog(newBox)
    }

    private fun updateBoxes() {
        val boxes = boxesRepository.getBoxesByProduct(productInfo.value!!).mapIndexed { index, box ->
            BoxListItem(
                number = (index + 1).toString(),
                title = "${box.code.take(5)}...${box.code.takeLast(5)} // ${box.stamps.first().dateOfPour}",
                subtitle = box.stamps.first().manufacturerName
            )
        }

        boxList.postValue(boxes)
    }

}