package com.lenta.bp12.features.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.GoodType
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.QuantityType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import java.util.*
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: ICreateTaskManager


    val title by lazy {
        "005588 Макароны с изюмом"
    }

    val good by lazy {
        manager.getCurrentGood()
    }

    val rollbackVisibility = MutableLiveData(true)

    val detailsVisibility = MutableLiveData(true)

    val missingVisibility = MutableLiveData(true)

    val rollbackEnabled = MutableLiveData(false)

    val missingEnabled = MutableLiveData(false)

    val applyEnabled = MutableLiveData(true)

    val ui by lazy {
        good.value?.let { good ->
            MutableLiveData(GoodInfoUi(
                    units = Uom.DEFAULT,
                    unitsName = Uom.DEFAULT.name.toLowerCase(Locale.getDefault()),
                    totalQuantity = "1 шт",
                    basketQuantity = "1 шт",
                    totalQuantityTitle = "",
                    basketQuantityTitle = "",
                    isFullMode = true,
                    matrixType = MatrixType.Active,
                    section = "02",
                    goodType = GoodType.COMMON,
                    isBasket = true,
                    isBoxScan = true,
                    isQrScan = true
            ))
        }
    }

    val quantityType = good.map { good ->
        when (good?.type) {
            GoodType.ALCOHOL -> "Партионно"
            GoodType.EXCISE -> "Партионно"
            else -> "Количество"
        }
    }

    val quantity = good.map { good ->
        good?.quantity.dropZeros()
    }

    val quantityTypeIcon = good.map { good ->
        when (good?.type) {
            GoodType.COMMON -> QuantityType.QUANTITY
            else -> QuantityType.MARK
        }
    }

    val quantityEnabled = MutableLiveData(true)

    val date = MutableLiveData("")

    val dateEnabled = MutableLiveData(true)

    val quantityTypeEnabled = MutableLiveData(true)

    val providerEnabled = MutableLiveData(true)

    val importerEnabled = MutableLiveData(true)

    val quantityTypePosition = MutableLiveData(0)

    val providerPosition = MutableLiveData(0)

    val importerPosition = MutableLiveData(0)

    val onSelectQuantityType = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            quantityTypePosition.value = position
        }
    }

    val onSelectProvider = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            providerPosition.value = position
        }
    }

    val onSelectImporter = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            importerPosition.value = position
        }
    }

    val quantityTypeList: MutableLiveData<List<String>> by lazy {
        MutableLiveData(List(3) {
            "QuantityType ${it + 1}"
        })
    }

    val providerList: MutableLiveData<List<String>> by lazy {
        MutableLiveData(List(3) {
            "Provider ${it + 1}"
        })
    }

    val importerList: MutableLiveData<List<String>> by lazy {
        MutableLiveData(List(3) {
            "Importer ${it + 1}"
        })
    }

    // -----------------------------

    fun onClickRollback() {

    }

    fun onClickDetails() {

    }

    fun onClickMissing() {

    }

    fun onClickApply() {

    }

    fun addProvider() {

    }

}

data class GoodInfoUi(
        val units: Uom,
        val unitsName: String,
        val totalQuantity: String,
        val basketQuantity: String,
        val totalQuantityTitle: String,
        val basketQuantityTitle: String,
        val isFullMode: Boolean,
        val matrixType: MatrixType,
        val section: String,
        val goodType: GoodType,
        val isBasket: Boolean,
        val isBoxScan: Boolean,
        val isQrScan: Boolean
)