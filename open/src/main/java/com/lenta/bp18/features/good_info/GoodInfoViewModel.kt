package com.lenta.bp18.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.features.other.SendDataViewModel
import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.platform.Constants
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.ConditionInfo
import com.lenta.shared.requests.combined.scan_info.pojo.GroupInfo
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : SendDataViewModel(), OnPositionClickListener {

    @Inject
    lateinit var appSettings: IAppSettings

    val deviceIp = MutableLiveData("")

    val selectedEan = MutableLiveData("")
    var weight = MutableLiveData(0)

    val quantityField: MutableLiveData<String> = MutableLiveData("")
    val partNumberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToQuantityField: MutableLiveData<Boolean> = MutableLiveData(true)
    val requestFocusToPartNumberField: MutableLiveData<Boolean> = MutableLiveData(true)

    val partNumberFieldEnabled: MutableLiveData<Boolean> = MutableLiveData(false)

    private val groups: MutableLiveData<List<GroupInfo>> = MutableLiveData()
    private val conditions: MutableLiveData<List<ConditionInfo>> = MutableLiveData()
    private val selectedPosition: MutableLiveData<Int> = MutableLiveData()

    val groupsNames: MutableLiveData<List<String>> = groups.map { group ->
        group?.map { it.name }.orEmpty()
    }

    val conditionNames: MutableLiveData<List<String>> = conditions.map { condition ->
        condition?.map { it.name }.orEmpty()
    }

    init {
        launchUITryCatch {
            val good = database.getGoodByEan(selectedEan.value.toString())
            val uom: String
            val quantity: Int?
            if (weight.value != 0) {
                quantity = weight.value?.div(Constants.CONVERT_TO_KG)
                uom = Uom.KG.name
            } else {
                when (good?.uom) {
                    Uom.ST -> {
                        quantity = Constants.QUANTITY_DEFAULT_VALUE_1
                        uom = Uom.ST.name
                    }
                    Uom.KAR -> {
                        val uomInfo = database.getEanInfoByEan(good.ean)
                        quantity = uomInfo?.umrez?.div(uomInfo.umren)
                                ?: Constants.QUANTITY_DEFAULT_VALUE_0
                        uom = Uom.KAR.name
                    }
                    else -> {
                        quantity = Constants.QUANTITY_DEFAULT_VALUE_0
                        uom = Constants.UNKNOWN_UOM
                    }
                }
            }

            quantityField.value = "$quantity $uom"

            /*Тут на самом деле не matcode, а ШК по индикатору (10). Осталось понять как его получить*/
            partNumberField.value = good?.matcode.orEmpty()

            val groupList = database.getAllGoodGroup()

            groups.value = groupList
            selectedPosition.value?.let {
                appSettings.lastGroup?.let { lGroup ->
                    groupList.forEachIndexed() { index, groupInfo ->
                        if (groupInfo.number == lGroup)
                            onClickPosition(index)
                        return@forEachIndexed
                    }
                }.orIfNull { onClickPosition(0) }
            }

            val conditionList = database.getAllGoodCondition()

            conditions.value = conditionList
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    fun onClickBack() {
        navigator.showConfirmSaveData {
            navigator.openSelectMarketScreen()
        }
    }

    fun onClickComplete() {
        navigator.showConfirmOpeningPackage {
            /*Подтверждение вскрытия упаковки*/
        }
    }
}