package com.lenta.bp18.features.good_info

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.bp18.request.model.params.GoodInfoParams
import com.lenta.bp18.request.network.GoodInfoNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_yyyyMMdd
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ConditionInfo
import com.lenta.shared.requests.combined.scan_info.pojo.GroupInfo
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

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
    val weight = MutableLiveData(DEFAULT_WEIGHT)
    val partNumberField: MutableLiveData<String> = MutableLiveData("")
    val quantityField: MutableLiveData<String> = MutableLiveData(DEF_WEIGHT)

    val requestFocusToQuantityField: MutableLiveData<Boolean> = MutableLiveData(true)
    val requestFocusToPartNumberField: MutableLiveData<Boolean> = MutableLiveData(true)

    val partNumberFieldEnabled: MutableLiveData<Boolean> = MutableLiveData(true)

    /**Выбор группы весового оборудования*/
    private val groups: MutableLiveData<List<GroupInfo>> = MutableLiveData()
    val groupsNames: MutableLiveData<List<String>> = groups.map { groups ->
        groups?.mapNotNull { it.name }.orEmpty()
    }
    val selectedGroup = MutableLiveData(0)

    /**Выбор условий хранения*/
    private val conditions: MutableLiveData<List<ConditionInfo>> = MutableLiveData()
    val conditionNames: MutableLiveData<List<String>> = conditions.map { conditions ->
        conditions?.mapNotNull { it.name }.orEmpty()
    }
    val selectedCondition = MutableLiveData(0)

    private val selectedGood = MutableLiveData<Good>()

    private var currentGroup: String? = ""
    private var currentCondition: ConditionInfo by Delegates.notNull()

    val suffix: MutableLiveData<String> = MutableLiveData(Uom.KG.name)
    private val buom = MutableLiveData<String>()

    val completeButtonEnabled = partNumberField.map { !it.isNullOrBlank() }

    init {
        setGoodInfo()
    }

    private fun setGoodInfo() = launchUITryCatch {
        database.getGoodByEan(selectedEan.value.toString())?.let { good ->
            Logg.d { "$good" }

            val conditionList = database.getConditionByName(good.getFormattedMatcode())
            if (conditionList.isEmpty()) {
                with(navigator) {
                    showAlertConditionNotFound(::openSelectGoodScreen)
                }
            } else {
                applyGoodForGroupAndCondition(good, conditionList)
                getServerTime()
            }

        } ?: navigator.showAlertGoodsNotFound()
    }

    private suspend fun applyGoodForGroupAndCondition(good: Good, conditionList: List<ConditionInfo>) {
        selectedGood.value = good
        conditions.value = conditionList

        getQuantityFieldFromGood(good)

        val marketNumber = sessionInfo.market.orEmpty()
        val groupList = database.getGroupToSelectedMarket(marketNumber)
        val filteredGroupList = filterGroupList(groupList)
        selectedGroup.value = findSelectedIndexForGroup(filteredGroupList)
        selectedCondition.value = findSelectedIndexForCondition(conditionList)
    }

    private suspend fun filterGroupList(groupList: List<GroupInfo>): List<GroupInfo> = withContext(Dispatchers.IO) {
        val currentMarket = sessionInfo.market
        groupList.filter { it.werks == currentMarket }
    }

    private suspend fun getQuantityFieldFromGood(good: Good) {
        val weightValue = weight.value
        val (quantity: Double?, uom: Uom) = if (weightValue != DEFAULT_WEIGHT) {
            weightValue?.div(Constants.CONVERT_TO_KG) to Uom.KG
        } else {
            when (good.uom) {
                Uom.ST -> Constants.QUANTITY_DEFAULT_VALUE_1 to Uom.ST
                Uom.KAR -> {
                    val uomInfo = database.getEanInfoByEan(good.ean)
                    val uomDiv = uomInfo?.umrez?.div(uomInfo.umren.toDouble())
                            ?: Constants.QUANTITY_DEFAULT_VALUE_0
                    uomDiv to Uom.KAR
                }
                else -> {
                    Constants.QUANTITY_DEFAULT_VALUE_0 to Uom.KG
                }
            }
        }

        quantityField.value = quantity.toString()
        suffix.value = uom.name
        buom.value = uom.code
    }

    private suspend fun findSelectedIndexForGroup(groupList: List<GroupInfo>): Int = withContext(Dispatchers.IO) {
        var selectedIndex = 0
        groups.postValue(groupList)
        appSettings.lastGroup?.let { lGroup ->
            groupList.forEachIndexed { index, groupInfo ->
                if (groupInfo.number == lGroup) {
                    currentGroup = groupInfo.number
                    selectedIndex = index
                    return@forEachIndexed
                }
            }
        }
        selectedIndex
    }

    private suspend fun findSelectedIndexForCondition(conditionList: List<ConditionInfo>): Int = withContext(Dispatchers.IO) {
        var selectedIndex = 0
        conditionList.forEachIndexed { index, conditionInfo ->
            if (conditionInfo.defCondition.isSapTrue()) {
                currentCondition = conditionInfo
                selectedIndex = index
                return@forEachIndexed
            }
        }
        selectedIndex
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
        launchUITryCatch {
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
                onConfirmationYesHandler()
            }

    private fun onConfirmationYesHandler() = launchUITryCatch {
        navigator.showProgressLoadingData()
        val dateOpen = timeMonitor.getServerDate().getFormattedDate(DATE_FORMAT_yyyyMMdd)
        val guid = UUID.randomUUID().toString().toUpperCase(Locale.getDefault())
        val result = goodInfoNetRequest(
                params = GoodInfoParams(
                        marketNumber = sessionInfo.market.orEmpty(),
                        sapCode = selectedGood.value?.getFormattedMatcode(),
                        grNum = currentGroup,
                        stdCond = currentCondition.number.orEmpty(),
                        quantity = quantityField.value,
                        buom = buom.value.orEmpty(),
                        partNumber = partNumberField.value,
                        guid = guid,
                        dateOpen = dateOpen,
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

    companion object {
        private const val DEF_WEIGHT = "0"
        private const val DEFAULT_WEIGHT = 0.0
    }
}