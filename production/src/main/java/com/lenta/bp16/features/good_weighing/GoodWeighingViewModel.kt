package com.lenta.bp16.features.good_weighing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.data.IPrinter
import com.lenta.bp16.data.IScales
import com.lenta.bp16.data.PrintInnerTagInfo
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.pojo.Pack
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.IGeneralRepository
import com.lenta.bp16.request.PackCodeNetRequest
import com.lenta.bp16.request.PackCodeParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GoodWeighingViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var taskManager: ITaskManager
    @Inject
    lateinit var packCodeNetRequest: PackCodeNetRequest
    @Inject
    lateinit var scales: IScales
    @Inject
    lateinit var printer: IPrinter
    @Inject
    lateinit var repository: IGeneralRepository


    val good by lazy {
        taskManager.currentGood
    }

    val raw by lazy {
        taskManager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val deviceIp = MutableLiveData("")

    val weightField = MutableLiveData("0")

    private val entered = weightField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    private val weighted = MutableLiveData<Double>(0.0)

    private val total = entered.map {
        it.sumWith(weighted.value ?: 0.0)
    }

    val totalWithUnits = total.map {
        "${it.dropZeros()} ${good.value!!.units.name}"
    }

    val planned by lazy {
        "${raw.value!!.planned.dropZeros()} ${good.value!!.units.name}"
    }

    val completeEnabled: MutableLiveData<Boolean> = total.map {
        it ?: 0.0 != 0.0
    }

    val addEnabled: MutableLiveData<Boolean> = entered.map {
        it ?: 0.0 != 0.0
    }

    // -----------------------------

    fun onClickComplete() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            packCodeNetRequest(
                    PackCodeParams(
                            marketNumber = sessionInfo.market ?: "Not found!",
                            taskType = taskManager.getTaskTypeCode(),
                            parent = taskManager.currentTask.value!!.taskInfo.number,
                            deviceIp = deviceIp.value ?: "Not found!",
                            material = good.value!!.material,
                            orderNumber = raw.value!!.orderNumber,
                            quantity = total.value!!
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { packCodeResult ->
                good.value?.let {
                    it.packs.add(0,
                            Pack(
                                    material = it.material,
                                    materialOsn = raw.value!!.materialOsn,
                                    code = packCodeResult.packCode,
                                    quantity = total.value!!
                            )
                    )

                    good.value = it
                }

                viewModelScope.launch {
                    val productTime = Calendar.getInstance()
                    productTime.add(Calendar.MINUTE, repository.getPcpExpirTimeMm())

                    val planAufFinish = Calendar.getInstance()
                    productTime.add(Calendar.MINUTE, repository.getPcpContTimeMm(packCodeResult.dataLabel.planAufFinish))

                    val dateExpir = packCodeResult.dataLabel.dateExpiration.toIntOrNull()?.let { days ->
                        val dateExpiration = Calendar.getInstance()
                        dateExpiration.add(Calendar.DAY_OF_YEAR, days)
                        dateExpiration
                    }

                    val barCodeText = "(01)${getFormattedEan(packCodeResult.dataLabel.ean, total.value!!)}" +
                            "(3103)${getFormattedWeight(weightField.value!!)}" +
                            "(8008)${SimpleDateFormat(Constants.DATE_FORMAT_yyMMddhhmm, Locale.getDefault()).format(productTime.time)}" +
                            "(10)${raw.value!!.orderNumber}" +
                            "(7003)${dateExpir?.let { SimpleDateFormat(Constants.DATE_FORMAT_yyMMddhhmm, Locale.getDefault()).format(it.time) }}" +
                            "(91)${packCodeResult.packCode}"

                    val barcode = barCodeText.replace("(", "").replace(")", "")

                    printTag(PrintInnerTagInfo(
                            quantity = "${total.value!!}  ${good.value?.units?.name}",
                            codeCont = packCodeResult.packCode,
                            storCond = "${packCodeResult.dataLabel.storCondTime} ч",
                            planAufFinish = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(planAufFinish.time),
                            aufnr = raw.value!!.orderNumber,
                            nameOsn = raw.value!!.name,
                            dateExpir = dateExpir?.let { SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(it.time) }
                                    ?: "",
                            goodsName = packCodeResult.dataLabel.materialName,
                            weigher = appSettings.weightEquipmentName ?: "",
                            productTime = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(productTime.time),
                            nameDone = packCodeResult.dataLabel.materialNameDone,
                            goodsCode = packCodeResult.dataLabel.material.takeLast(6),
                            barcode = barcode,
                            barcodeText = barCodeText
                    ))

                    total.value = 0.0
                    weightField.value = "0"

                    navigator.openPackListScreen()
                }
            }
        }
    }

    fun getFormattedWeight(weight: String): String {
        if (weight.isEmpty()) {
            return "000000"
        }

        val dividedWeight = weight.split(".")

        var kilogram = dividedWeight[0]
        while (kilogram.length < 3) {
            kilogram = "0$kilogram"
        }

        var gram = if (dividedWeight.size == 1) "0" else dividedWeight[1]
        while (gram.length < 3) {
            gram = "${gram}0"
        }

        return "$kilogram$gram"
    }

    fun getFormattedEan(sourceEan: String, quantity: Double): String {
        val ean = sourceEan.take(7)
        var weight = (quantity * 1000).toInt().toString()

        while (weight.length < 5) {
            weight = "0$weight"
        }
        while (weight.length > 5) {
            weight = weight.dropLast(1)
        }

        /*Логика расчета контрольного числа:
        1. Складываются цифры, находящихся на четных позициях;
        2. Полученный результат умножается на три;
        3. Складываются цифры, находящиеся на нечётных позициях;
        4. Складываются результаты по п.п. 2 и 3;
        5. Определяется ближайшее наибольшее число к п. 4, кратное десяти;
        6. Определяется разность между результатами по п.п. 5 и 4;*/

        val nums = weight.toCharArray().map { it.toString().toInt() }
        val sum = ((nums[1] + nums[3]) * 3) + (nums[0] + nums[2] + nums[4])
        var nearestMultipleOfTen = sum
        while (nearestMultipleOfTen % 10 != 0) {
            nearestMultipleOfTen += 1
        }

        val controlNumber = nearestMultipleOfTen - sum

        return "$ean$weight$controlNumber"
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    fun onClickAdd() {
        weighted.value = total.value!!
        weightField.value = "0"
    }

    fun onClickGetWeight() {
        viewModelScope.launch {
            withContext(IO) {
                navigator.showProgressLoadingData()
                scales.getWeight().also {
                    navigator.hideProgress()
                }.either(::handleFailure) { weight ->
                    weightField.postValue(weight)
                }
            }
        }
    }

    private fun printTag(printInfo: PrintInnerTagInfo) {
        viewModelScope.launch {
            withContext(IO) {
                appSettings.printerIpAddress.let { ipAddress ->
                    if (ipAddress == null) {
                        return@let null
                    }
                    printer.printTag(printInfo, ipAddress)
                            .either(::handleFailure) {
                                // todo Что-то делаем после печати?
                            }
                }

            }.also {
                if (it == null) {
                    navigator.showAlertNoIpPrinter()
                }
            }
        }
    }

    fun onBackPressed() {
        if (entered.value ?: 0.0 != 0.0 || weighted.value ?: 0.0 != 0.0) {
            navigator.showNotSavedDataWillBeLost {
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
    }

}