package com.lenta.bp9.features.goods_information.non_excise_sets_pge.set_component_pge

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val setInfo: MutableLiveData<TaskSetsInfo> = MutableLiveData()
    val planQuantityBatch: MutableLiveData<String> = MutableLiveData()
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
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val enabledResetButton: MutableLiveData<Boolean> = MutableLiveData(false)

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) > 0.0
    }

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    fun getTitle() : String {
        return  "${setInfo.value!!.componentNumber.substring(setInfo.value!!.componentNumber.length - 6)} ${zfmpUtz48V001.getProductInfoByMaterial(setInfo.value!!.componentNumber)?.name}"
    }

    init {
        viewModelScope.launch {
            suffix.value = setInfo.value?.uom?.name
            qualityInfo.value = dataBase.getQualityInfoPGE()

            spinQuality.value = listOf(qualityInfo.value?.findLast {find ->
                find.code == typeDiscrepancies.value
            }.let {
                it?.name ?: ""
            })

            spinProcessingUnit.value = listOf("ЕО - ${productInfo.value!!.processingUnit}")

            batchInfo.value = taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(setInfo.value!!.componentNumber)

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
        val manufactureCode = manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(spinManufacturersSelectedPosition.value ?: 0)
        }?.code
        val batchSelected = batchInfo.value?.findLast {batch ->
            batch.egais == manufactureCode &&
                    batch.bottlingDate == formatterEN.format(formatterRU.parse(spinBottlingDate.value?.get(spinBottlingDateSelectedPosition.value ?: 0))) &&
                    batch.processingUnitNumber == spinProcessingUnit.value?.get(spinProcessingUnitSelectedPosition.value ?: 0)?.substring(5)
        }

        if (batchSelected != null) {
            if (processNonExciseSetsPGEProductService.overLimitBatch(countValue.value!!, batchSelected)) {
                screenNavigator.openAlertOverLimitPlannedBatchScreen()
            } else {
                processNonExciseSetsPGEProductService.addComponent(count.value!!, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinProcessingUnit.value!![spinProcessingUnitSelectedPosition.value!!].substring(5), batchSelected)
            }
        }
    }

    override fun onClickPosition(position: Int) {
        spinProcessingUnitSelectedPosition.value = position
    }

    fun onClickPositionSpinManufacturers(position: Int) {
        spinManufacturersSelectedPosition.value = position
        updateDataSpinBottlingDate(position)
    }

    private fun updateDataSpinBottlingDate(position: Int) {
        val manufactureCode = manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(position)
        }?.code

        val bottlingDates = batchInfo.value?.filter {
            it.egais == manufactureCode
        }?.map {batch ->
            formatterRU.format(formatterEN.parse(batch.bottlingDate))
        }
        spinBottlingDateSelectedPosition.value = 0
        spinBottlingDate.value = bottlingDates

        batchInfo.value?.findLast {batch ->
            batch.egais == manufactureCode && batch.bottlingDate == formatterEN.format(formatterRU.parse(spinBottlingDate.value?.get(spinBottlingDateSelectedPosition.value ?: 0)))
        }?.let {
            planQuantityBatch.value = "${it.purchaseOrderScope.toStringFormatted()} ${setInfo.value!!.uom.name}"
        }
        updateDataSpinProcessingUnit(spinBottlingDateSelectedPosition.value ?: 0)

    }

    private fun updateDataSpinProcessingUnit(positionSpinBottlingDate: Int) {
        val manufactureCode = manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(spinManufacturersSelectedPosition.value ?: 0)
        }?.code

        val bottlingDate = formatterEN.format(formatterRU.parse(spinBottlingDate.value?.get(positionSpinBottlingDate)))

        val listProcessingUnitNumber = batchInfo.value?.filter {fIt ->
            fIt.egais == manufactureCode && fIt.bottlingDate == bottlingDate
        }?.groupBy {gIt ->
            gIt.processingUnitNumber
        }?.map {mIt ->
            "ЕО - " + mIt.key
        }
        spinProcessingUnitSelectedPosition.value = 0
        spinProcessingUnit.value = listProcessingUnitNumber
    }


    fun onClickPositionBottlingDate(position: Int) {
        spinBottlingDateSelectedPosition.value = position
        batchInfo.value?.let {
            planQuantityBatch.value = "${it[position].purchaseOrderScope.toStringFormatted()} ${setInfo.value!!.uom.name}"
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
}
