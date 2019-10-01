package com.lenta.bp14.models.work_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.work_list.repo.WorkListRepo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.util.*

class WorkListTask(
        private val workListRepo: WorkListRepo,
        private val taskDescription: WorkListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : IWorkListTask {

    //private var currentList = processed

    val processing = MutableLiveData<MutableList<Good>>(mutableListOf())
    val processed = MutableLiveData<MutableList<Good>>(mutableListOf())
    val search = MutableLiveData<MutableList<Good>>(mutableListOf())

    var currentGood = MutableLiveData<Good>()

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

    override suspend fun loadSalesStatistics() {
        delay(500)
        currentGood.value?.let { good ->
            val salesStatistics = workListRepo.loadSalesStatistics(good)
            good.sales.value = salesStatistics
        }
    }

    override suspend fun loadDeliveries() {
        delay(500)
        currentGood.value?.let { good ->
            val deliveriesList = workListRepo.loadDeliveries(good)
            good.deliveries.value = deliveriesList
        }
    }

    override suspend fun loadComments() {
        delay(500)
        currentGood.value?.let { good ->
            val commentsList = workListRepo.loadComments(good)
            good.comments.value = commentsList
        }
    }

    /*fun setCurrentList(tabPosition: Int) {
        currentList = when (tabPosition) {
            GoodsListTab.PROCESSED.position -> processed
            GoodsListTab.PROCESSING.position -> processing
            GoodsListTab.SEARCH.position -> search
            else -> processed
        }
    }*/

    override fun getGoodOptions(): LiveData<GoodOptions> {
        return currentGood.map { it?.common?.options }
    }

    override fun getGoodStocks(): LiveData<List<Stock>> {
        return currentGood.map { it?.additional?.value?.stocks?.toList() }
    }

    override fun getGoodProviders(): LiveData<List<Provider>> {
        return currentGood.map { it?.additional?.value?.providers?.toList() }
    }

    override fun getTaskType(): ITaskType {
        return TaskTypes.WorkList.taskType
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
            currentGood.value?.let {good ->
                if (!list.contains(good)) {
                    list.add(good)
                    processed.value = list
                }
            }
        }

        processing.value?.let { list ->
            currentGood.value?.let {good ->
                if (list.contains(good)) {
                    list.remove(good)
                    processing.value = list
                }
            }
        }
    }

}


interface IWorkListTask : ITask {
    suspend fun addGoodByEan(ean: String): Boolean

    suspend fun loadAdditionalGoodInfo()
    suspend fun loadSalesStatistics()
    suspend fun loadDeliveries()
    suspend fun loadComments()

    fun addScanResult(scanResult: ScanResult)
    fun moveGoodToProcessedList()

    fun getGoodOptions(): LiveData<GoodOptions>
    fun getGoodStocks(): LiveData<List<Stock>>
    fun getGoodProviders(): LiveData<List<Provider>>
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
        val ean: String,
        val material: String,
        val matcode: String,
        val name: String,
        val units: Uom,
        val defaultQuantity: BigDecimal,
        var goodGroup: String,
        var purchaseGroup: String,
        var marks: Int = 0,
        val shelfLife: Int,
        val options: GoodOptions
)

data class AdditionalGoodInfo(
        val storagePlaces: String,
        val minStock: BigDecimal,
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
        val quantity: BigDecimal
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
        val commonPrice: BigDecimal,
        val discountPrice: BigDecimal
)

data class Promo(
        val name: String,
        val period: String
)

// -----------------------------

data class SalesStatistics(
        val lastSaleDate: Date,
        val daySales: Int,
        val weekSales: Int,
        val units: Uom
) {

    fun getDaySalesWithUnits(): String {
        return "$daySales ${units.name.toLowerCase(Locale.getDefault())}"
    }

    fun getWeekSalesWithUnits(): String {
        return "$weekSales ${units.name.toLowerCase(Locale.getDefault())}"
    }

}

data class Delivery(
        val status: DeliveryStatus,
        val info: String, // ПП, РЦ, ...
        val quantity: BigDecimal,
        val units: Uom,
        val date: Date
) {

    fun getQuantityWithUnits(): String {
        return "$quantity ${units.name.toLowerCase(Locale.getDefault())}"
    }

}

enum class DeliveryStatus(val description: String) {
    ON_WAY("В пути"),
    ORDERED("Заказан")
}

// -----------------------------

data class ScanResult(
        val quantity: BigDecimal,
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