package com.lenta.bp18.features.good_info

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.bp18.request.network.GoodInfoNetRequest
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ConditionInfo
import com.lenta.shared.requests.combined.scan_info.pojo.GroupInfo
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var database: IDatabaseRepo

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    val selectedEan = MutableLiveData("")
    var weight = MutableLiveData(0)

    val quantityField: MutableLiveData<String> = MutableLiveData(DEF_WEIGHT)
    val partNumberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToQuantityField: MutableLiveData<Boolean> = MutableLiveData(true)
    val requestFocusToPartNumberField: MutableLiveData<Boolean> = MutableLiveData(true)

    val partNumberFieldEnabled: MutableLiveData<Boolean> = MutableLiveData(true)

    private val groups: MutableLiveData<List<GroupInfo>> = MutableLiveData()
    private val conditions: MutableLiveData<List<ConditionInfo>> = MutableLiveData()
    private val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    private val selectedCondition: MutableLiveData<Int> = MutableLiveData()

    val groupsNames: MutableLiveData<List<String?>> = groups.map { group ->
        group?.map { it.name }.orEmpty()
    }

    val conditionNames: MutableLiveData<List<String?>> = conditions.map { condition ->
        condition?.map { it.name }.orEmpty()
    }

    var currentCondition: String? = ""

    var suffix: String = Uom.KG.name

    val completeButtonEnabled = partNumberField.map { !it.isNullOrBlank() }

    init {
        setGoodInfo()
    }

    private fun setGoodInfo() {
        launchUITryCatch {
            val good = database.getGoodByEan(selectedEan.value.toString())
            Logg.d { "$good" }
            val (quantity: Int?, uom: String?) = if (weight.value != 0) {
                weight.value?.div(Constants.CONVERT_TO_KG) to Uom.KG.name
            } else {
                when (good?.uom) {
                    Uom.ST -> Constants.QUANTITY_DEFAULT_VALUE_1 to Uom.ST.name
                    Uom.KAR -> {
                        val uomInfo = database.getEanInfoByEan(good.ean)
                        val uomDiv = uomInfo?.umrez?.div(uomInfo.umren)
                                ?: Constants.QUANTITY_DEFAULT_VALUE_0
                        uomDiv to Uom.KAR.name
                    }
                    else -> {
                        Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.KG.name
                    }
                }
            }

            quantityField.value = quantity.toString()
            suffix = uom
            /*ШК по индикатору (10) для GS1, для EAN13 не заполнять*/
            //partNumberField.value = /*значение*/

            val groupList = database.getAllGoodGroup()
            /**Добавить выборку групп*/
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

            database.getConditionByName(good?.matcode).let { list ->
                conditions.value = list.map { ConditionInfo(name = it.name) }
                selectedCondition.value?.let {
                    list.forEachIndexed { index, conditionInfo ->
                        if (conditionInfo.defCondition == DEF_COND_FLAG) {
                            currentCondition = conditionInfo.name
                            onClickCondition(index)
                        }
                        else{
                            onClickCondition(0)
                        }
                    }
                }
            }

        }

    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    fun onClickCondition(position: Int) {
        selectedCondition.value = position
    }

    fun onClickBack() {
        with(navigator) {
            showConfirmSaveData {
                openSelectGoodScreen()
            }
        }
    }

    fun onClickComplete() {
        with(navigator) {
            showConfirmOpeningPackage {
                showAlertSuccessfulOpeningPackage(::openSelectGoodScreen)
            }
        }
    }

    companion object {
        private const val DEF_WEIGHT = "0"
        private const val DEF_COND_FLAG = "X"
    }

}