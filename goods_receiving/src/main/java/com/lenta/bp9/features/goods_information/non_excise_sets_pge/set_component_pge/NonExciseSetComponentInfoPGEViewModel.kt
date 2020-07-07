package com.lenta.bp9.features.goods_information.non_excise_sets_pge.set_component_pge

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.processing.ProcessNonExciseSetsPGEProductService
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
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

class NonExciseSetComponentInfoPGEViewModel : CoreViewModel(),
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
    lateinit var processNonExciseSetsPGEProductService: ProcessNonExciseSetsPGEProductService

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var context: Context

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
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
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
        viewModelScope.launch {
            suffix.value = setInfo.value?.uom?.name
            qualityInfo.value = dataBase.getQualityInfoPGE()

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
                    batch.processingUnitNumber == spinProcessingUnit.value?.get(spinProcessingUnitSelectedPosition.value
                    ?: 0)?.substring(context.getString(R.string.spin_processing_unit).length)
        }

        if (batchSelected != null && count.value != null && qualityInfo.value != null && setInfo.value != null) {
            processNonExciseSetsPGEProductService.addCurrentComponent(
                    count = count.value!!,
                    typeDiscrepancies = qualityInfo.value!![spinQualitySelectedPosition.value
                            ?: 0].code,
                    componentInfo = setInfo.value!!,
                    batchInfo = batchSelected
            )
        }
        screenNavigator.goBack()
    }

    override fun onClickPosition(position: Int) {
        spinProcessingUnitSelectedPosition.value = position
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
                                planQuantityBatch.value = processNonExciseSetsPGEProductService.getCountSet() * set.quantity - allCountDiscrepancies - processNonExciseSetsPGEProductService.getCountDiscrepanciesOfComponent(set.componentNumber) - (count.value?.toDoubleOrNull()
                                        ?: 0.0)
                                planQuantityBatchWithUom.value = "${(batch.purchaseOrderScope - allCountDiscrepancies - processNonExciseSetsPGEProductService.getCountDiscrepanciesOfComponent(set.componentNumber) - (count.value?.toDoubleOrNull() ?: 0.0)).toStringFormatted()} ${set.uom.name}"
                            }
                }
            }
        }
        updateDataSpinProcessingUnit(spinBottlingDateSelectedPosition.value ?: 0)

    }

    private fun updateDataSpinProcessingUnit(positionSpinBottlingDate: Int) {
        val bottlingDate = spinBottlingDate.value?.let {
            formatterEN.format(formatterRU.parse(it[positionSpinBottlingDate]))
        }

        val listProcessingUnitNumber = batchInfo.value?.filter { fIt ->
            fIt.egais == getManufactureCode(spinManufacturersSelectedPosition.value
                    ?: 0) && fIt.bottlingDate == bottlingDate
        }?.groupBy { gIt ->
            gIt.processingUnitNumber
        }?.map { mIt ->
            "${context.getString(R.string.spin_processing_unit)}${mIt.key}"
        }
        spinProcessingUnitSelectedPosition.value = 0
        spinProcessingUnit.value = listProcessingUnitNumber
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
                            planQuantityBatch.value = processNonExciseSetsPGEProductService.getCountSet() * set.quantity - allCountDiscrepancies - processNonExciseSetsPGEProductService.getCountDiscrepanciesOfComponent(set.componentNumber) - (count.value?.toDoubleOrNull()
                                    ?: 0.0)
                            planQuantityBatchWithUom.value = "${(batches[position].purchaseOrderScope - allCountDiscrepancies - processNonExciseSetsPGEProductService.getCountDiscrepanciesOfComponent(set.componentNumber) - (count.value?.toDoubleOrNull() ?: 0.0)).toStringFormatted()} ${set.uom.name}"
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
