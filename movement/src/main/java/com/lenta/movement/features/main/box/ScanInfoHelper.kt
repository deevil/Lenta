package com.lenta.movement.features.main.box

import com.lenta.movement.models.ProductInfo
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.ScanInfoNetRequest
import com.lenta.movement.requests.network.ScanInfoParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

class ScanInfoHelper @Inject constructor(
    private val screenNavigator: IScreenNavigator,
    private val sessionInfo: ISessionInfo,
    private val scanInfoNetRequest: ScanInfoNetRequest
) {

    companion object {
        private const val SAP_RC = 160
        private const val BAR_RC = 170
    }

    private enum class CodeType {
        SAP,
        BAR
    }

    private var resultCodeTypeDeferred: CompletableDeferred<CodeType>? = null

    fun handleFragmentResult(code: Int?): Boolean {
        val result = when (code) {
            SAP_RC -> CodeType.SAP
            BAR_RC -> CodeType.BAR
            else -> return false
        }
        resultCodeTypeDeferred?.complete(result)

        return true
    }

    suspend fun searchCode(
        code: String,
        fromScan: Boolean,
        isBarCode: Boolean? = null,
        onResult: ((ProductInfo) -> Unit)? = null
    ) {
        val codeType = if (isBarCode == null && code.length == 12) {
            selectCodeType()
        } else if (isBarCode == true) {
            CodeType.BAR
        } else {
            CodeType.SAP
        }

        if (codeType == null) return

        val number = when (codeType) {
            CodeType.SAP -> "000000000000${code.takeLast(6)}"
            else -> code
        }

        screenNavigator.showProgress(scanInfoNetRequest)

        val scanCodeInfo = ScanCodeInfo(number, if (fromScan) null else 0.0)
        scanInfoNetRequest(
            params = ScanInfoParams(
                ean = scanCodeInfo.eanNumberForSearch ?: "",
                tk = sessionInfo.market.orEmpty(),
                matNr = scanCodeInfo.materialNumberForSearch ?: "",
                codeEBP = "MVM"
            )
        ).either(
            fnL = { failure ->
                screenNavigator.openAlertScreen(failure)
            },
            fnR = {
                onResult?.invoke(it) ?: Unit
            }
        )
        screenNavigator.hideProgress()
    }

    private suspend fun selectCodeType(): CodeType? {
        screenNavigator.openSelectTypeCodeScreen(SAP_RC, BAR_RC)
        resultCodeTypeDeferred = CompletableDeferred()

        return resultCodeTypeDeferred?.await()
    }
}