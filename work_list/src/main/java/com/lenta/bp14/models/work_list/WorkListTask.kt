package com.lenta.bp14.models.work_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lenta.bp14.di.WorkListScope
import com.lenta.bp14.models.BaseProductInfo
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.work_list.repo.IWorkListRepo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject

@WorkListScope
class WorkListTask @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val workListRepo: IWorkListRepo,
        private val taskDescription: WorkListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : IWorkListTask {

    override val processing = MutableLiveData<MutableList<Good>>(mutableListOf())
    override val processed = MutableLiveData<MutableList<Good>>(mutableListOf())
    override val search = MutableLiveData<MutableList<Good>>(mutableListOf())

    override var currentGood = MutableLiveData<Good>()

    override suspend fun addGoodByEan(ean: String): Boolean {
        processed.value?.find { it.common.ean == "12345678" }?.let { good ->
            currentGood.value = good
            return true
        }

        workListRepo.getCommonGoodInfoByEan(ean)?.let { commonInfo ->
            val good = Good(common = commonInfo)
            val processingList = processing.value!!
            processingList.add(good)
            processing.value = processingList
            currentGood.value = good
            loadComments()
            return true
        }

        return false
    }

    override fun addScanResult(scanResult: ScanResult) {
        val scanResultsList = currentGood.value?.scanResults?.value?.toMutableList()
        scanResultsList?.add(scanResult)
        currentGood.value?.scanResults?.value = scanResultsList
    }

    override suspend fun loadAdditionalGoodInfo() {
        delay(5000)
        currentGood.value?.let { good ->
            val additionalGoodInfo = workListRepo.loadAdditionalGoodInfo(good)
            good.additional.value = additionalGoodInfo
        }
    }

    override suspend fun loadComments() {
        delay(500)
        currentGood.value?.let { good ->
            val commentsList = workListRepo.loadComments(good)
            good.comments.value = commentsList
        }
    }

    override fun getGoodOptions(): LiveData<GoodOptions> {
        return currentGood.map { it?.common?.options }
    }

    override fun getGoodStocks(): LiveData<List<Stock>> {
        return currentGood.map { it?.additional?.value?.stocks?.toList() }
    }

    override fun getGoodProviders(): LiveData<List<Provider>> {
        return currentGood.map { it?.additional?.value?.providers?.toList() }
    }

    override fun getTaskType(): ITaskTypeInfo {
        return generalRepo.getTasksTypeInfo(AppTaskTypes.WorkList.taskType)!!
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    fun deleteScanResultsByComments(comments: List<String>) {
        val scanResultsList = currentGood.value?.scanResults?.value?.toMutableList()
        scanResultsList?.removeAll { comments.contains(it.comment) }
        currentGood.value?.scanResults?.value = scanResultsList
    }

    fun deleteScanResultsByShelfLives(shelfLives: List<String>) {
        val scanResultsList = currentGood.value?.scanResults?.value?.toMutableList()
        scanResultsList?.removeAll { shelfLives.contains(it.getFormattedProductionDate() + it.getFormattedExpirationDate()) }
        currentGood.value?.scanResults?.value = scanResultsList
    }

    override fun moveGoodToProcessedList() {
        processed.value?.let { list ->
            currentGood.value?.let { good ->
                if (!list.contains(good)) {
                    list.add(good)
                    processed.value = list
                }
            }
        }

        processing.value?.let { list ->
            currentGood.value?.let { good ->
                if (list.contains(good)) {
                    list.remove(good)
                    processing.value = list
                }
            }
        }
    }

    override fun isEmpty(): Boolean {
        return processed.value.isNullOrEmpty()
    }

    override fun isHaveDiscrepancies(): Boolean {
        //TODO implement this
        return false
    }

    override fun getListOfDifferences(): LiveData<List<BaseProductInfo>> {
        //TODO implement this
        return MutableLiveData(emptyList())
    }

    override fun setMissing(matNrList: List<String>) {
        //TODO implement this
    }

    override suspend fun getUnitsName(code: String?): String? {
        return workListRepo.getUnitsName(code)
    }

}


interface IWorkListTask : ITask {
    val processing: MutableLiveData<MutableList<Good>>
    val processed: MutableLiveData<MutableList<Good>>
    val search: MutableLiveData<MutableList<Good>>
    var currentGood: MutableLiveData<Good>

    suspend fun addGoodByEan(ean: String): Boolean

    suspend fun loadAdditionalGoodInfo()
    suspend fun loadComments()

    fun addScanResult(scanResult: ScanResult)
    fun moveGoodToProcessedList()

    fun getGoodOptions(): LiveData<GoodOptions>
    fun getGoodStocks(): LiveData<List<Stock>>
    fun getGoodProviders(): LiveData<List<Provider>>
    suspend fun getUnitsName(code: String?): String?
}

// -----------------------------

data class Good(
        val common: CommonGoodInfo,
        var additional: MutableLiveData<AdditionalGoodInfo> = MutableLiveData(),
        val sales: MutableLiveData<SalesStatistics> = MutableLiveData(),
        val deliveries: MutableLiveData<List<Delivery>> = MutableLiveData(listOf()),
        val comments: MutableLiveData<List<String>> = MutableLiveData(listOf()),

        val scanResults: MutableLiveData<List<ScanResult>> = MutableLiveData(listOf())
) {

    fun getFormattedMaterialWithName(): String {
        return "${common.material.takeLast(6)} ${common.name}"
    }

    fun isCommonGood(): Boolean {
        return common.options.goodType == GoodType.COMMON
    }

    fun getEanWithUnits(): String? {
        return "${common.ean}/${common.units.name}"
    }

    fun getGoodWithPurchaseGroups(): String? {
        return "${common.goodGroup}/${common.purchaseGroup}"
    }

    fun getShelfLifeInMills(): Long {
        return (common.shelfLife * 24 * 60 * 60 * 1000).toLong()
    }

    fun getUnits(): String {
        return common.units.name.toLowerCase(Locale.getDefault())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Good) return false

        if (common.ean != other.common.ean) return false

        return true
    }

    override fun hashCode(): Int {
        return common.ean.hashCode()
    }

}


data class CommonGoodInfo(
        val ean: String?,
        val material: String,
        val name: String,
        val units: Uom,
        val defaultQuantity: Double,
        var goodGroup: String,
        var purchaseGroup: String,
        var marks: Int = 0,
        val shelfLife: Int,
        val options: GoodOptions
)

data class AdditionalGoodInfo(
        val storagePlaces: String,
        val minStock: Double,
        val movement: Movement,
        val price: Price,
        val promo: Promo,
        val providers: MutableList<Provider>,
        val stocks: MutableList<Stock>
)

data class GoodOptions(
        val matrixType: MatrixType,
        val goodType: GoodType,
        val section: String,
        val healthFood: Boolean = false,
        val novelty: Boolean = false
)

data class Stock(
        val number: Int,
        val storage: String,
        val quantity: Double
)

data class Provider(
        val number: Int,
        val code: String,
        val name: String,
        val kipStart: Date,
        val kipEnd: Date
)

data class Movement(
        val inventory: String,
        val arrival: String
)

data class Price(
        val commonPrice: Double,
        val discountPrice: Double
)

data class Promo(
        val name: String,
        val period: String
)

// -----------------------------

data class SalesStatistics(
        val lastSaleDate: Date?,
        val daySales: Double,
        val weekSales: Double
)

data class Delivery(
        val status: String,
        val type: String,
        val quantity: Double,
        val date: Date?
)

// -----------------------------

data class ScanResult(
        val quantity: Double,
        val comment: String,
        val productionDate: Date?,
        val expirationDate: Date?
) {

    fun getFormattedProductionDate(): String {
        return if (productionDate != null) "ДП ${productionDate.getFormattedDate()}" else ""
    }

    fun getFormattedExpirationDate(): String {
        return if (expirationDate != null) "СГ ${expirationDate.getFormattedDate()}" else ""
    }

    fun getKeyFromDates(): String {
        return "${productionDate?.time}${expirationDate?.time}"
    }

}