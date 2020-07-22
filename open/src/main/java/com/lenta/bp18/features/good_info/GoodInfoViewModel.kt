package com.lenta.bp18.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.model.pojo.*
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.ConditionInfo
import com.lenta.shared.requests.combined.scan_info.pojo.GroupInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var database: IDatabaseRepo

    val deviceIp = MutableLiveData("")

    /**Чисто для теста*/
    val barcode = "2499999000000"
    var quantity = 0
    var partNumber = ""

    val quantityField: MutableLiveData<String> = MutableLiveData()
    val partNumberField: MutableLiveData<String> = MutableLiveData()

    private val weightValue = listOf("23", "24", "27", "28")

    val goods: MutableLiveData<Good> = MutableLiveData()
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
            val good = database.getGoodInfoByEan(barcode)

            quantity = when {
                weightValue.contains(good?.ean?.substring(0 until 2)) -> {
                    barcode.takeLast(6).take(5).toInt() / 1000
                }
                good?.uom == Uom.ST -> {
                    1
                }
                good?.uom == Uom.KAR -> {
                    val ean = database.getEanInfoByEan(barcode)
                    ean?.umrez?.div(ean.umren) ?: 0
                }
                else -> 0
            }
            partNumber = good?.matcode ?: ""
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

            database.getAllGoodCondition().let { matnr ->
                conditions.value = matnr
            }
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    fun onClickBack() {
        navigator.showConfirmSaveData()
    }

    fun onClickComplete() {
        navigator.showConfirmOpeningPackage()
    }
}