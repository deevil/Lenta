package com.lenta.bp9.features.goods_information.sets.task_ppp.set_component_receiving

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.processing.ProcessNonExciseSetsReceivingProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskSetsInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import java.text.SimpleDateFormat
import javax.inject.Inject

class NonExciseSetComponentInfoReceivingViewModel : CoreViewModel(),
        OnPositionClickListener {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var processNonExciseSetsReceivingProductService: ProcessNonExciseSetsReceivingProductService

    @Inject
    lateinit var hyperHive: HyperHive

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val setInfo: MutableLiveData<TaskSetsInfo> = MutableLiveData()
    private val planQuantityBatch: MutableLiveData<Double> = MutableLiveData()
    val planQuantityBatchWithUom: MutableLiveData<String> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinBottlingDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinBottlingDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinAlcocode: MutableLiveData<List<String>> = MutableLiveData()
    val spinAlcocodeSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val typeDiscrepancies: MutableLiveData<String> = MutableLiveData()

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val batchInfo: MutableLiveData<List<TaskBatchInfo>> = MutableLiveData()
    private val manufacturer: MutableLiveData<List<Manufacturer>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map {
        if (it != null && spinManufacturersSelectedPosition.value != null) {
            updateDataSpinBottlingDate(spinManufacturersSelectedPosition.value!!)
        }
        it?.toDoubleOrNull() ?: 0.0
    }

    val enabledResetButton: MutableLiveData<Boolean> = MutableLiveData(false)

    val enabledApplyButton: MutableLiveData<Boolean> = planQuantityBatch.combineLatest(countValue).map {
        (it?.first ?: 0.0) >= 0.0 && (it?.second ?: 0.0) > 0.0
    }

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    fun getTitle(): String {
        return setInfo.value?.let {
            "${it.componentNumber.substring(it.componentNumber.length - 6)} ${zfmpUtz48V001.getProductInfoByMaterial(it.componentNumber)?.name}"
        } ?: ""
    }

    init {
        launchUITryCatch {
            suffix.value = setInfo.value?.uom?.name
            qualityInfo.value = dataBase.getQualityInfo()

            spinQuality.value = listOf(qualityInfo.value?.findLast { find ->
                find.code == typeDiscrepancies.value
            }.let {
                it?.name ?: ""
            })

            batchInfo.value = setInfo.value?.let {
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatches()
                        ?.findBatchOfProduct(it.componentNumber)
            }

            manufacturer.value = batchInfo.value?.mapNotNull { batch ->
                repoInMemoryHolder.manufacturers.value?.findLast {
                    it.code == batch.egais
                }
            }

            spinManufacturers.value = manufacturer.value?.groupBy {
                it.name
            }?.map {
                it.key
            }
        }
    }

    fun onClickReset() {
        enabledResetButton.value = false
    }

    fun onClickApply() {
        val bottlingDate = spinBottlingDate.value?.let {
            formatterEN.format(formatterRU.parse(it[spinBottlingDateSelectedPosition.value ?: 0]))
        }
        val batchSelected = batchInfo.value?.findLast { batch ->
            batch.egais == getManufactureCode(spinManufacturersSelectedPosition.value ?: 0) &&
                    batch.bottlingDate == bottlingDate &&
                    batch.alcocode == spinAlcocode.value?.get(spinAlcocodeSelectedPosition.value
                    ?: 0)
        }

        if (batchSelected != null && count.value != null && setInfo.value != null && typeDiscrepancies.value != null) {
            processNonExciseSetsReceivingProductService.addCurrentComponent(
                    count = count.value!!,
                    typeDiscrepancies = typeDiscrepancies.value!!,
                    componentInfo = setInfo.value!!,
                    batchInfo = batchSelected
            )
        }
        screenNavigator.goBack()
    }

    override fun onClickPosition(position: Int) {
        spinAlcocodeSelectedPosition.value = position
    }

    fun onClickPositionSpinManufacturers(position: Int) {
        spinManufacturersSelectedPosition.value = position
        updateDataSpinBottlingDate(position)
    }

    private fun updateDataSpinBottlingDate(position: Int) {
        val manufactureCode = getManufactureCode(position)

        val bottlingDates = batchInfo.value?.filter {
            it.egais == manufactureCode
        }?.map { batch ->
            formatterRU.format(formatterEN.parse(batch.bottlingDate))
        }
        spinBottlingDateSelectedPosition.value = 0
        spinBottlingDate.value = bottlingDates

        spinBottlingDate.value?.let { bottlingDate ->
            batchInfo.value?.findLast { batch ->
                batch.egais == manufactureCode && batch.bottlingDate == formatterEN.format(formatterRU.parse(bottlingDate[spinBottlingDateSelectedPosition.value
                        ?: 0]))
            }?.let { batch ->
                setInfo.value?.let { set ->
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getAllCountDiscrepanciesOfProduct(set.componentNumber)
                            ?.let { allCountDiscrepancies ->
                                planQuantityBatch.value = batch.purchaseOrderScope - allCountDiscrepancies - processNonExciseSetsReceivingProductService.getCountDiscrepanciesOfComponent(set.componentNumber) - (count.value?.toDoubleOrNull()
                                        ?: 0.0)
                                planQuantityBatchWithUom.value = "${planQuantityBatch.value.toStringFormatted()} ${set.uom.name}"
                            }

                }
            }
        }
        updateDataSpinAlcocode(spinBottlingDateSelectedPosition.value ?: 0)
    }

    private fun updateDataSpinAlcocode(positionSpinBottlingDate: Int) {
        val bottlingDate = spinBottlingDate.value?.let {
            formatterEN.format(formatterRU.parse(it[positionSpinBottlingDate]))
        }

        val listAlcocode = batchInfo.value?.filter { batches ->
            batches.egais == getManufactureCode(spinManufacturersSelectedPosition.value
                    ?: 0) && batches.bottlingDate == bottlingDate
        }?.groupBy { batch ->
            batch.alcocode
        }?.map {
            it.key
        }
        spinAlcocodeSelectedPosition.value = 0
        spinAlcocode.value = listAlcocode
    }


    fun onClickPositionBottlingDate(position: Int) {
        spinBottlingDateSelectedPosition.value = position
        batchInfo.value?.let { batches ->
            setInfo.value?.let { set ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.getAllCountDiscrepanciesOfProduct(set.componentNumber)
                        ?.let { allCountDiscrepancies ->
                            planQuantityBatch.value = batches[position].purchaseOrderScope - allCountDiscrepancies - processNonExciseSetsReceivingProductService.getCountDiscrepanciesOfComponent(set.componentNumber) - (count.value?.toDoubleOrNull()
                                    ?: 0.0)
                            planQuantityBatchWithUom.value = "${planQuantityBatch.value.toStringFormatted()} ${set.uom.name}"
                        }
            }
        }
    }

    fun onClickPositionSpinQuality(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    fun onScanResult(data: String) {
        val alcocode = batchInfo.value?.findLast {
            it.alcocode == data
        }

        if (alcocode == null) {
            screenNavigator.openAlertAlcocodeNotFoundTaskScreen()
        } else {
            enabledResetButton.value = true
        }
    }

    private fun getManufactureCode(position: Int): String {
        return manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(position)
        }
                ?.code
                ?: ""
    }
}
