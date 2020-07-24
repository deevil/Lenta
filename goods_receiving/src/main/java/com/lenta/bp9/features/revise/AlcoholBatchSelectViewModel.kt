package com.lenta.bp9.features.revise

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.ProductDocumentType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class AlcoholBatchSelectViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var hyperHive: HyperHive

    private val ZfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val taskCaption: String by lazy {
        (taskManager.getReceivingTask()?.taskHeader?.topText?.split("//")?.first() ?: "") + " // " +
                matnr.takeLast(6) + " " +
                (ZfmpUtz48V001.getProductInfoByMaterial(matnr)?.name ?: "")
    }

    lateinit var matnr: String
    lateinit var type: ProductDocumentType

    val batches: MutableLiveData<List<BatchVM>> = MutableLiveData()

    private fun updateBatches() {
        val batchesData = taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getProductBatches()?.filter { it.productNumber == matnr }
        batchesData?.let {
            batches.value = it.mapIndexed { index, batch ->
                BatchVM(position = (it.size - index).toString(),
                        topText = batch.text1,
                        bottomText = batch.text2,
                        quantityString = batch.quantity.toString(),
                        isChecked = batch.isCheck,
                        batchNumber = batch.batchNumber
                )
            }
        }
    }

    fun onResume() {
        updateBatches()
    }

    fun onClickItemPosition(position: Int) {
        batches.value?.get(position)?.let {
            when (type) {
                ProductDocumentType.AlcoImport -> {
                    screenNavigator.openImportAlcoFormReviseScreen(matnr, it.batchNumber)
                }
                ProductDocumentType.AlcoRus -> {
                    screenNavigator.openRussianAlcoFormReviseScreen(matnr, it.batchNumber)
                }
            }
        }
    }
}

data class BatchVM(
        val position: String,
        val topText: String,
        val bottomText: String,
        val quantityString: String,
        val isChecked: Boolean,

        val batchNumber: String
)
