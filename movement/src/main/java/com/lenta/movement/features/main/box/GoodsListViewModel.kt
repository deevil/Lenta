package com.lenta.movement.features.main.box

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequestParams
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    companion object {
        private const val SAP_RC = 160
        private const val BAR_RC = 170
    }

    private enum class CodeType {
        SAP,
        BAR
    }

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var scanInfoRequest: ScanInfoRequest

    private var resultCodeTypeDeferred: CompletableDeferred<CodeType>? = null

    val selectionsHelper = SelectionItemsHelper()

    val goodsList: MutableLiveData<List<GoodListItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    override fun handleFragmentResult(code: Int?): Boolean {
        val result = when (code) {
            SAP_RC -> CodeType.SAP
            BAR_RC -> CodeType.BAR
            else -> return false
        }
        resultCodeTypeDeferred?.complete(result)

        return true
    }

    fun onScanResult(data: String) {
        searchCode(code = data, fromScan = true, isBarCode = true)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCode(eanCode.value.orEmpty(), fromScan = false)

        return true
    }

    fun onClickItemPosition(position: Int) {
        //TODO("Not yet implemented")
    }

    fun onBackPressed() {
        screenNavigator.openUnsavedDataDialog(
            yesCallbackFunc = {
                screenNavigator.goBack()
                screenNavigator.goBack()
            }
        )
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        viewModelScope.launch {
            val codeType = if (isBarCode == null && code.length == 12) {
                selectCodeType()
            } else if (isBarCode == true) {
                CodeType.BAR
            } else {
                CodeType.SAP
            }

            if (codeType == null) return@launch

            screenNavigator.showProgress(scanInfoRequest)
            scanInfoRequest(
                ScanInfoRequestParams(
                    number = when (codeType) {
                        CodeType.SAP -> "000000000000${code.takeLast(6)}"
                        else -> code
                    },
                    tkNumber = sessionInfo.market.orEmpty(),
                    fromScan = fromScan,
                    isBarCode = codeType == CodeType.BAR
                )
            )
                .either(
                    fnL = { failure ->
                        screenNavigator.openAlertScreen(failure)
                    },
                    fnR = { scanInfoResult ->
                        if (scanInfoResult.productInfo.type != ProductType.NonExciseAlcohol) {
                            screenNavigator.openProductIncorrectForCreateBox(scanInfoResult.productInfo)
                        } else {
                            screenNavigator.openCreateBoxByProduct(scanInfoResult.productInfo)
                        }
                    }
                )
            screenNavigator.hideProgress()
        }
    }

    private suspend fun selectCodeType(): CodeType? {
        screenNavigator.openSelectTypeCodeScreen(SAP_RC, BAR_RC)
        resultCodeTypeDeferred = CompletableDeferred()

        return resultCodeTypeDeferred?.await()
    }

}