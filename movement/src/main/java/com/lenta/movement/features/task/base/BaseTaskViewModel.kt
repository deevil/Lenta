package com.lenta.movement.features.task.base

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class BaseTaskViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    @Inject
    protected lateinit var screenNavigator: IScreenNavigator

    @Inject
    protected lateinit var taskManager: ITaskManager

    val eanCode: MutableLiveData<String> = MutableLiveData()

    fun onScanResult(data: String) {
        searchCode(code = data, fromScan = true, isBarCode = true)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        val eanCodeValue = eanCode.value
        return if (eanCodeValue != null && eanCodeValue.isNotEmpty()) {
            searchCode(eanCodeValue, fromScan = false)
            true
        } else {
            false
        }
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        launchUITryCatch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                onSearchResult(productInfo)
            }
        }
    }

    fun onSearchResult(productInfo: ProductInfo) = launchUITryCatch {
        val isAllowed = withContext(Dispatchers.IO) { taskManager.isAllowProduct(productInfo) }
        if (isAllowed) showProductInfoScreen(productInfo)
        else showProductBannedMessage()
    }

    open fun showProductInfoScreen(productInfo: ProductInfo) {
        screenNavigator.openTaskGoodsInfoScreen(productInfo)
    }

    private fun showProductBannedMessage() {
        screenNavigator.openBannedProductDialog()
    }
}