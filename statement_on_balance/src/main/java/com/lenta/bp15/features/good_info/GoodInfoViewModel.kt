package com.lenta.bp15.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        "000044 Туфли"
    }

    private val quantity = MutableLiveData("0")

    val quantityWithUnits by lazy {
        quantity.map {
            val quantity = it.toIntOrNull() ?: 0
            "$quantity шт"
        }
    }

    val goodInfo by lazy {
        GoodInfoUi(
                markType = "Обувь женская",
                matrix = MatrixType.Active,
                section = "02"
        )
    }

    private val totalToProcessing = 1000

    private val processed by lazy {
        MutableLiveData(521)
    }

    val processedOf by lazy {
        processed.map {
            "$it из $totalToProcessing"
        }
    }

    val allMarkProcessed by lazy {
        quantity.map {
            val quantity = it.toIntOrNull() ?: 0
            quantity == totalToProcessing
        }
    }

    val rollbackEnabled = MutableLiveData(false)

    val applyEnabled = MutableLiveData(false)

    fun onClickRollback() {

    }

    fun onClickApply() {

    }

    fun onScanResult(data: String) {

    }

    fun onBackPressed() {

    }

}