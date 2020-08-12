package com.lenta.bp18.features.good_info

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.bp18.request.model.params.GoodInfoParams
import com.lenta.bp18.request.network.GoodInfoNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ConditionInfo
import com.lenta.shared.requests.combined.scan_info.pojo.GroupInfo
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var database: IDatabaseRepo

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    val selectedEan = MutableLiveData("")
    var weight = MutableLiveData(0)

    val quantityField: MutableLiveData<String> = MutableLiveData(DEF_WEIGHT)
    val partNumberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToQuantityField: MutableLiveData<Boolean> = MutableLiveData(true)
    val requestFocusToPartNumberField: MutableLiveData<Boolean> = MutableLiveData(true)

    val partNumberFieldEnabled: MutableLiveData<Boolean> = MutableLiveData(true)


    /**Выбор группы весового оборудования*/
    private val groups: MutableLiveData<List<GroupInfo>> = MutableLiveData()
    val groupsNames: MutableLiveData<List<String?>> = groups.map { groups ->
        groups?.map { it.name }.orEmpty()
    }
    val selectedGroup = MutableLiveData(0)

    val selectedGroupClickListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedGroup.value = position
        }

    }

    /**Выбор условий хранения*/
    private val conditions: MutableLiveData<List<ConditionInfo>> = MutableLiveData()
    val conditionNames: MutableLiveData<List<String?>> = conditions.map { conditions ->
        conditions?.map { it.name }.orEmpty()
    }
    val selectedCondition = MutableLiveData(0)
    val selectedConditionClickListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedCondition.value = position
        }

    }

    private val selectedGood = MutableLiveData<Good>()

    private var currentGroup: String? = ""
    private var currentCondition: String? = ""

    var suffix: String = Uom.KG.name

    val completeButtonEnabled = partNumberField.map { !it.isNullOrBlank() }

    init {
        setGoodInfo()
        getServerTime()
    }

    private fun setGoodInfo() {
        launchUITryCatch {
            val good = database.getGoodByEan(selectedEan.value.toString())
            selectedGood.value = good
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

            selectedGroup.value = findSelectedIndexForGroup(groupList)

            val conditionList = database.getConditionByName(good?.getFormattedMatcode())
            if (conditionList.isEmpty()) {
                with(navigator) {
                    showAlertConditionNotFound(::openSelectGoodScreen)
                }
            }

            selectedCondition.value = findSelectedIndexForCondition(conditionList)
        }
    }

    private fun findSelectedIndexForGroup(groupList: List<GroupInfo>): Int {
        var selectedIndex = 0
        groups.value = groupList
        appSettings.lastGroup?.let { lGroup ->
            groupList.forEachIndexed { index, groupInfo ->
                if (groupInfo.number == lGroup) {
                    currentGroup = groupInfo.number
                    selectedIndex = index
                    return@forEachIndexed
                }
            }
        }
        return selectedIndex
    }

    private fun findSelectedIndexForCondition(conditionList: List<ConditionInfo>): Int {
        var selectedIndex = 0
        conditions.value = conditionList
        conditionList.forEachIndexed { index, conditionInfo ->
            if (conditionInfo.defCondition == DEF_COND_FLAG) {
                currentCondition = conditionInfo.name
                selectedIndex = index
                return@forEachIndexed
            }
        }
        return selectedIndex
    }

    fun onClickBack() {
        saveGroup()
        with(navigator) {
            showConfirmSaveData {
                openSelectGoodScreen()
            }
        }
    }

    private fun saveGroup() {
        launchUITryCatch {
            groups.value
                    ?.getOrNull(selectedGroup.value ?: -1)
                    ?.number
                    ?.let { group ->
                        appSettings.lastGroup = group
                    }
        }
    }

    private fun getServerTime() {
        viewModelScope.launch {
            serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                    .orEmpty())).either(::handleFailure, ::handleSuccessServerTime)
        }
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
    }

    fun onClickComplete() =
            navigator.showConfirmOpeningPackage {
                saveGroup()
                launchUITryCatch {
                    navigator.showProgressLoadingData()
                    val result = goodInfoNetRequest(
                            params = GoodInfoParams(
                                    marketNumber = sessionInfo.market.orEmpty(),
                                    sapCode = selectedGood.value?.getFormattedMatcode(),
                                    grNum = currentGroup,
                                    stdCond = currentCondition,
                                    quantity = quantityField.value,
                                    buom = suffix,
                                    partNumber = partNumberField.value,
                                    /**Уникальный идентификатор, потом заменить на генерацию GUID*/
                                    guid = DEF_GUID,
                                    dateOpen = timeMonitor.getServerDate().toString(),
                                    timeOpen = timeMonitor.getUnixTime().toString(),
                                    ean = selectedEan.value
                            )
                    )
                    result.also {
                        navigator.hideProgress()
                    }.either(::handleFailure) {
                        navigator.openSelectGoodScreen()
                    }
                }
            }

    companion object {
        private const val DEF_WEIGHT = "0"
        private const val DEF_COND_FLAG = "X"
        /**It's a temporary solution*/
        private const val DEF_GUID = "1"
    }
}