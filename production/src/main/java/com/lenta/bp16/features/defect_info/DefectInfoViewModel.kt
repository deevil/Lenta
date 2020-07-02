package com.lenta.bp16.features.defect_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.data.IPrinter
import com.lenta.bp16.data.IScales
import com.lenta.bp16.data.LabelInfo
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.pojo.Pack
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.IDatabaseRepository
import com.lenta.bp16.request.PackCodeNetRequest
import com.lenta.bp16.request.PackCodeParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.DictElement
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DefectInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ITaskManager

    @Inject
    lateinit var scales: IScales

    @Inject
    lateinit var printer: IPrinter

    @Inject
    lateinit var packCodeNetRequest: PackCodeNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var database: IDatabaseRepository


    val good by lazy {
        manager.currentGood
    }

    val raw by lazy {
        manager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val deviceIp = MutableLiveData("")

    val weightField = MutableLiveData("0")

    private val entered = weightField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    private val weighted = MutableLiveData(0.0)

    private val total = entered.map {
        it.sumWith(weighted.value ?: 0.0)
    }

    val totalWithUnits = total.map {
        "${it.dropZeros()} ${good.value!!.units.name}"
    }

    private val categories = MutableLiveData<List<DictElement>>(emptyList())

    val categoryEnabled = categories.map {
        it?.size ?: 0 > 1 && weighted.value!! == 0.0
    }

    val categoryList = categories.map { list ->
        list?.map { it.description }
    }

    val categoryPosition = MutableLiveData(0)

    val onSelectCategory = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            categoryPosition.value = position
        }
    }

    private val defects = MutableLiveData<List<DictElement>>(emptyList())

    val defectEnabled = defects.map {
        it?.size ?: 0 > 1 && weighted.value!! == 0.0
    }

    val defectList = defects.map { list ->
        list?.map { it.description }
    }

    val defectPosition = MutableLiveData(0)

    val onSelectDefect = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            defectPosition.value = position
        }
    }

    val addEnabled: MutableLiveData<Boolean> = entered.map {
        it ?: 0.0 != 0.0
    }

    val labelEnabled: MutableLiveData<Boolean> = total.combineLatest(categoryPosition).combineLatest(defectPosition).map {
        val quantity = it?.first?.first ?: 0.0
        val categoryPosition = it?.first?.second ?: 0
        val defectPosition = it?.second ?: 0

        val isQuantityNotNull = quantity != 0.0
        val isCategorySelected = categories.value!!.size == 1 || categories.value!!.size > 1 && categoryPosition != 0
        val isDefectSelected = defects.value!!.size == 1 || defects.value!!.size > 1 && defectPosition != 0

        isQuantityNotNull && isCategorySelected && isDefectSelected
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            categories.value = database.getCategoryList()
            defects.value = database.getDefectList()
        }
    }

    // -----------------------------

    fun onBackPressed() {
        if (entered.value ?: 0.0 != 0.0 || weighted.value ?: 0.0 != 0.0) {
            navigator.showNotSavedDataWillBeLost {
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
    }
    
    fun onClickDetails() {
        navigator.openDefectListScreen()
    }

    fun onClickGetWeight() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                navigator.showProgressLoadingData()
                scales.getWeight().also {
                    navigator.hideProgress()
                }.either(::handleFailure) { weight ->
                    weightField.postValue(weight)
                }
            }
        }
    }

    fun onClickAdd() {
        weighted.value = total.value!!
        weightField.value = "0"
    }

    fun onClickLabel() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            packCodeNetRequest(
                    PackCodeParams(
                            marketNumber = sessionInfo.market ?: "Not found!",
                            taskType = manager.getTaskTypeCode(),
                            parent = manager.currentTask.value!!.taskInfo.number,
                            deviceIp = deviceIp.value ?: "Not found!",
                            material = good.value!!.material,
                            order = raw.value!!.order,
                            quantity = total.value!!,
                            categoryCode = categories.value!![categoryPosition.value!!].code,
                            defectCode = defects.value!![defectPosition.value!!].code,
                            personnelNumber = sessionInfo.personnelNumber ?: ""
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { packCodeResult ->
                good.value?.let {good ->
                    good.packs.add(0,
                            Pack(
                                    material = good.material,
                                    materialOsn = raw.value!!.materialOsn,
                                    materialDef = raw.value!!.material,
                                    code = packCodeResult.packCode,
                                    order = raw.value!!.order,
                                    quantity = total.value!!,
                                    category = categories.value!![categoryPosition.value!!],
                                    defect = defects.value!![defectPosition.value!!]
                            )
                    )

                    manager.updateCurrentGood(good)
                    manager.onTaskChanged()
                }

                viewModelScope.launch {
                    val productTime = Calendar.getInstance()
                    productTime.add(Calendar.MINUTE, database.getPcpExpirTimeMm())

                    val planAufFinish = Calendar.getInstance()
                    planAufFinish.add(Calendar.MINUTE, getTimeInMinutes(packCodeResult.dataLabel.planAufFinish, packCodeResult.dataLabel.planAufUnit))
                    planAufFinish.add(Calendar.MINUTE, database.getPcpContTimeMm())

                    val dateExpir = packCodeResult.dataLabel.time.toIntOrNull()?.let { time ->
                        val dateExpiration = Calendar.getInstance()
                        when (packCodeResult.dataLabel.timeType.toIntOrNull()) {
                            1 -> dateExpiration.add(Calendar.HOUR_OF_DAY, time)
                            2 -> dateExpiration.add(Calendar.DAY_OF_YEAR, time)
                        }

                        dateExpiration
                    }

                    val barCodeText = "(01)${getFormattedEan(packCodeResult.dataLabel.ean, total.value!!)}" +
                            "(91)${packCodeResult.packCode}"

                    val barcode = barCodeText.replace("(", "").replace(")", "")

                    try {
                        printLabel(LabelInfo(
                                quantity = "${total.value!!}  ${good.value?.units?.name}",
                                codeCont = packCodeResult.packCode,
                                planAufFinish = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(planAufFinish.time),
                                aufnr = raw.value!!.order,
                                nameOsn = raw.value!!.name,
                                dateExpir = dateExpir?.let { SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy, Locale.getDefault()).format(it.time) }
                                        ?: "",
                                goodsName = "***БРАК*** ${packCodeResult.dataLabel.materialName}",
                                weigher = sessionInfo.personnelNumber ?: "",
                                productTime = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy, Locale.getDefault()).format(productTime.time),
                                goodsCode = packCodeResult.dataLabel.material.takeLast(6),
                                barcode = barcode,
                                barcodeText = barCodeText,
                                printTime = Date()
                        ))
                    } catch (e: Exception) {
                        Logg.e { "Create print label exception: $e" }
                    }

                    weighted.value = 0.0
                    weightField.value = "0"
                }
            }
        }
    }

    private fun getTimeInMinutes(sourceTime: String, units: String): Int {
        return when (units.toLowerCase(Locale.getDefault())) {
            "m" -> (sourceTime.toDoubleOrNull() ?: 0.0).toInt()
            "h" -> (sourceTime.toDoubleOrNull() ?: 0.0 * 60).toInt()
            else -> 0
        }
    }

    private fun getFormattedEan(sourceEan: String, quantity: Double): String {
        val ean = sourceEan.take(7)
        var weight = (quantity * 1000).toInt().toString()

        while (weight.length < 5) {
            weight = "0$weight"
        }
        while (weight.length > 5) {
            weight = weight.dropLast(1)
        }

        val eanWithWeight = "$ean$weight"

        /*Логика расчета контрольного числа:
        1. Складываются цифры, находящихся на четных позициях;
        2. Полученный результат умножается на три;
        3. Складываются цифры, находящиеся на нечётных позициях;
        4. Складываются результаты по п.п. 2 и 3;
        5. Определяется ближайшее наибольшее число к п. 4, кратное десяти;
        6. Определяется разность между результатами по п.п. 5 и 4;*/

        val nums = eanWithWeight.toCharArray().map { it.toString().toInt() }

        var evenSum = 0
        var oddSum = 0

        for ((index, num) in nums.withIndex()) {
            if ((index + 1) % 2 == 0) evenSum += num else oddSum += num
        }

        val sum = (evenSum * 3) + oddSum
        var nearestMultipleOfTen = sum
        while (nearestMultipleOfTen % 10 != 0) {
            nearestMultipleOfTen += 1
        }

        val controlNumber = nearestMultipleOfTen - sum

        return "0$eanWithWeight$controlNumber"
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    private fun printLabel(labelInfo: LabelInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                manager.addLabelToList(labelInfo)

                appSettings.printerIpAddress.let { ipAddress ->
                    if (ipAddress == null) {
                        return@let null
                    }

                    navigator.showProgressLoadingData()

                    printer.printLabel(labelInfo, ipAddress)
                            .also {
                                navigator.hideProgress()
                            }.either(::handleFailure) {
                                // Ничего не делаем...
                            }
                }
            }.also {
                if (it == null) {
                    navigator.showAlertNoIpPrinter()
                }
            }
        }
    }

}
