package com.lenta.movement.features.main.box

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.models.repositories.IBoxesRepository
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.SavePackagedExciseBoxesNetRequest
import com.lenta.movement.requests.network.SavePackagedExciseBoxesParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    @Inject
    lateinit var boxesRepository: IBoxesRepository

    @Inject
    lateinit var savePackagedExciseBoxesNetRequest: SavePackagedExciseBoxesNetRequest

    val selectionsHelper = SelectionItemsHelper()

    val goodsList: MutableLiveData<List<GoodListItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val deleteEnabled: MutableLiveData<Boolean> =
        selectionsHelper.selectedPositions.map { selectedPositions ->
            selectedPositions.orEmpty().isNotEmpty()
        }

    val completeEnabled: MutableLiveData<Boolean> = goodsList.map { goodsListOrNull ->
        goodsListOrNull.orEmpty().isNotEmpty()
    }

    override fun handleFragmentResult(code: Int?): Boolean {
        return scanInfoHelper.handleFragmentResult(code) || super.handleFragmentResult(code)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCode(eanCode.value.orEmpty(), fromScan = false)

        return true
    }

    fun onScanResult(data: String) {
        searchCode(code = data, fromScan = true, isBarCode = true)
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    fun onResume() {
        updateGoodsList()
    }

    fun onBackPressed() {
        if (boxesRepository.getBoxes().isNotEmpty()) {
            screenNavigator.openUnsavedDataDialog(
                yesCallbackFunc = {
                    boxesRepository.clear()
                    screenNavigator.goBack()
                    screenNavigator.goBack()
                }
            )
        } else {
            screenNavigator.goBack()
        }
    }

    fun onDeleteClick() {
        selectionsHelper.selectedPositions.value.orEmpty()
            .flatMap { doRemovePositions ->
                boxesRepository.getBoxesGroupByProduct().toList()[doRemovePositions].second
            }
            .forEach { doRemoveBox ->
                boxesRepository.removeBox(doRemoveBox)
            }

        updateGoodsList()

        selectionsHelper.clearPositions()
    }

    fun onCompleteClick() {
        viewModelScope.launch {
            screenNavigator.showProgress(savePackagedExciseBoxesNetRequest)
            savePackagedExciseBoxesNetRequest(
                params = SavePackagedExciseBoxesParams(
                    userNumber = sessionInfo.personnelNumber ?: "",
                    deviceIp = context.getDeviceIp(),
                    boxes = boxesRepository.getBoxes().map { box ->
                        SavePackagedExciseBoxesParams.Box(
                            code = box.code,
                            materialNumber = box.productInfo.materialNumber
                        )
                    },
                    stamps = boxesRepository.getBoxes().flatMap { box ->
                        box.stamps.map { stamp ->
                            SavePackagedExciseBoxesParams.Stamp(
                                code = stamp.code,
                                boxCode = box.code
                            )
                        }
                    }
                )
            ).either(
                fnL = { failure ->
                    screenNavigator.openAlertScreen(failure)
                },
                fnR = {
                    boxesRepository.clear()
                    screenNavigator.goBack()
                }
            )
            screenNavigator.hideProgress()
        }
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        viewModelScope.launch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                if (productInfo.type != ProductType.ExciseAlcohol) {
                    screenNavigator.openProductIncorrectForCreateBox(productInfo)
                } else {
                    screenNavigator.openCreateBoxByProduct(productInfo)
                }
            }
        }
    }

    private fun updateGoodsList() {
        val goodsBoxesList = boxesRepository.getBoxesGroupByProduct()
            .toList()
            .mapIndexed { index, (product, boxes) ->
                GoodListItem(
                    number = index + 1,
                    name = product.description,
                    countWithUom = "${boxes.size} кор"
                )
            }

        goodsList.postValue(goodsBoxesList)
    }

}