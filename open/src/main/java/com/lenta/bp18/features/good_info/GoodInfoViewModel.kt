package com.lenta.bp18.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.features.other.SendDataViewModel
import com.lenta.bp18.platform.Constants
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.ConditionInfo
import com.lenta.shared.requests.combined.scan_info.pojo.GroupInfo
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : SendDataViewModel(), OnPositionClickListener {

    @Inject
    lateinit var appSettings: IAppSettings

    //val barcode = "2999999640343" //опилки
    //val barcode = "2425352000000" //не опилки
    val deviceIp = MutableLiveData("")

    var selectedEan = MutableLiveData("")
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
        group?.map { it.name }
    }

    val conditionNames: MutableLiveData<List<String>> = conditions.map { condition ->
        condition?.map { it.name }
    }

    init {
        viewModelScope.launch {
            val good = database.getGoodByEan(selectedEan.value.toString())
            Logg.d { "good.ean:${good?.ean}" }
            Logg.d { "Good uom:${good?.uom}, ${good?.material}, ${good?.name}, ${good?.uom}, ${weight.value}" }
            val quantity = when {
                weight.value != null -> {
                    weight.value?.div(Constants.CONVERT_TO_KG)
                }

                good?.uom == Uom.ST -> {
                    1
                }
                good?.uom == Uom.KAR -> {
                    val uom = database.getEanInfoByEan(good.ean)
                    uom?.umrez?.div(uom.umren) ?: "0"
                }
                else -> 0
            }

            quantityField.value = quantity.toString()

            partNumberField.value = good?.matcode.orEmpty()

            database.getAllGoodGroup().let { list ->
                groups.value = list
                if (selectedPosition.value == null) {
                    if (appSettings.lastGroup != null) {
                        list.forEachIndexed() { index, groupInfo ->
                            if (groupInfo.number == appSettings.lastGroup)
                                onClickPosition(index)
                        }
                    } else
                        onClickPosition(0)
                }
            }

            database.getAllGoodCondition().let { list ->
                conditions.value = list
            }
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