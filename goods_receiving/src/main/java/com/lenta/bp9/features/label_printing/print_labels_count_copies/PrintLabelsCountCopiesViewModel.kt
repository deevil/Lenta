package com.lenta.bp9.features.label_printing.print_labels_count_copies

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.features.label_printing.LabelPrintingZBatches
import com.lenta.bp9.features.label_printing.LabelZBatchesInfo
import com.lenta.bp9.features.label_printing.LabelPrintingItem
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.dao_ext.getEanInfoByMaterialUnits
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.ArrayList

class PrintLabelsCountCopiesViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var printer: LabelPrintingZBatches

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    private val labels: MutableLiveData<List<LabelPrintingItem>> = MutableLiveData()

    val  isVisibilityProductionDate: MutableLiveData<Boolean> = labels.map {
        it?.size == 1
    }

    val tvProductionDate: MutableLiveData<String> = isVisibilityProductionDate.map {
        if (it == true) {
            labels.value?.getOrNull(FIRST_LABEL)?.productionDate.orEmpty()
        } else {
            ""
        }
    }

    val countCopies: MutableLiveData<String> = MutableLiveData(DEFAULT_COUNT_COPiES)
    val tvPrintingByGoods: MutableLiveData<String> = MutableLiveData()

    val enabledConfirmBtn = countCopies.map {
        val countCopiesValue = it?.toIntOrNull() ?: 0
        countCopiesValue > 0
    }

    init {
        launchUITryCatch {
            tvPrintingByGoods.value =
                    if (isVisibilityProductionDate.value == true) {
                        "${context.getString(R.string.good)} ${labels.value?.getOrNull(FIRST_LABEL)?.productName.orEmpty()}"
                    } else {
                        context.getString(R.string.printing_by_goods)
                    }
        }
    }

    fun initLabels(_labels: List<LabelPrintingItem>) {
        labels.value = _labels
    }

    fun getTitle(): String {
        return taskManager
                .getReceivingTask()
                ?.taskHeader
                ?.caption
                .orEmpty()
    }

    fun onClickConfirm() {
        printingLabels()
    }

    private fun printingLabels() {
        launchUITryCatch {
            labels.value?.map { labelItem ->
                try {
                    val batchNumber = labelItem.batchDiscrepancies?.batchNumber.orEmpty()
                    val materialNumber = labelItem.batchDiscrepancies?.materialNumber.orEmpty()
                    val product = getProductInfoForLabel(materialNumber)
                    val eanInfo = ZmpUtz25V001(hyperHive).getEanInfoByMaterialUnits(materialNumber, product?.uom?.code.orEmpty())
                    val barCodeText = "(01)${eanInfo?.ean.orEmpty()}(10)$batchNumber"
                    val barcode = barCodeText.replace("(", "").replace(")", "")
                    val countCopiesValue = countCopies.value?.toIntOrNull() ?: 1
                    printLabel(LabelZBatchesInfo(
                            goodsName = product?.description.orEmpty(),
                            goodsCode = product?.getMaterialLastSix().toString(),
                            shelfLife = labelItem.shelfLife,
                            productTime = labelItem.productionDate,
                            delivery = taskManager.getReceivingTask()?.taskDescription?.deliveryNumber.orEmpty(),
                            provider = taskManager.getReceivingTask()?.taskDescription?.supplierName.orEmpty(),
                            batchNumber = batchNumber,
                            manufacturer = getManufacturerName(labelItem.batchDiscrepancies?.manufactureCode.orEmpty()),
                            weigher = sessionInfo.personnelNumber.orEmpty(),
                            quantity = labelItem.quantityUnit,
                            barcode = barcode,
                            barcodeText = barCodeText,
                            copies = countCopiesValue.toString()
                    ))
                } catch (e: Exception) {
                    Logg.e { "Create print label exception: $e" }
                }
            }
        }
    }

    private fun getProductInfoForLabel(materialNumber: String): TaskProductInfo? {
        return taskManager
                .getReceivingTask()
                ?.getProcessedProducts()
                ?.findLast { product -> product.materialNumber == materialNumber }
    }

    private fun getManufacturerName(manufacturerCode: String) : String {
        return repoInMemoryHolder
                .manufacturersForZBatches.value
                ?.findLast { it.manufactureCode == manufacturerCode }
                ?.manufactureName
                .orEmpty()
    }

    private fun goBackWithArgs() {
        val printedLabels: ArrayList<String> = ArrayList()
        labels.value?.mapTo(printedLabels) {it.copy().batchDiscrepancies?.batchNumber.orEmpty()}

        screenNavigator.goBackWithArgs(Bundle().apply {
            putStringArrayList("printedLabels", printedLabels)
        })
    }

    private fun printLabel(labelInfo: LabelZBatchesInfo) {
        launchUITryCatch {
            withContext(Dispatchers.IO) {
                appSettings.printerIpAddress.let { ipAddress ->
                    if (ipAddress == null) {
                        return@let null
                    }

                    screenNavigator.showProgressLoadingData(::handleFailure)

                    printer.printLabel(labelInfo, ipAddress)
                            .also {
                                screenNavigator.hideProgress()
                                goBackWithArgs()
                            }.either(::handleFailure) {
                                screenNavigator.goBack()
                            }
                }
            }.also {
                if (it == null) {
                    screenNavigator.showAlertNoIpPrinter()
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_COUNT_COPiES = "1"
        private const val FIRST_LABEL = 0
        private const val UNIT_G = "G"
    }
}
