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
import java.util.*

class WorkListTask(
        private val workListRepo: WorkListRepo,
        private val taskDescription: WorkListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : IWorkListTask {

    //val processing: MutableList<Good> = mutableListOf()
    //val processed: MutableList<Good> = mutableListOf()
    //val search: MutableList<Good> = mutableListOf()
    //private var currentList = processed

    val goods = MutableLiveData<MutableList<Good>>(mutableListOf())

    var currentGood = MutableLiveData<Good>()

    val sales = MutableLiveData<SalesStatistics>()
    val deliveries = MutableLiveData<List<Delivery>>(listOf())
    val comments = MutableLiveData<List<String>>(listOf())

    override suspend fun addGoodByEan(ean: String): Boolean {
        var good = goods.value?.find { it.common.ean == ean }
        if (good != null) {
            currentGood.value = good
            return true
        }

        val commonGoodInfo = workListRepo.getCommonGoodInfoByEan(ean)
        if (commonGoodInfo != null) {
            good = Good(common = commonGoodInfo)

            val goodsList = goods.value!!
            goodsList.add(good)
            goods.value = goodsList
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

        val good = currentGood.value
        if (good != null) {
            val additionalGoodInfo = workListRepo.loadAdditionalGoodInfo(good)
            good.additional = additionalGoodInfo
            currentGood.value = good
        }
    }

    override suspend fun loadSalesStatistics() {
        delay(500)

        val good = currentGood.value
        if (good != null) {
            val salesStatistics = workListRepo.loadSalesStatistics(good)
            sales.value = salesStatistics
        }
    }

    override suspend fun loadDeliveries() {
        delay(500)

        val good = currentGood.value
        if (good != null) {
            val deliveriesList = workListRepo.loadDeliveries(good)
            deliveries.value = deliveriesList
        }
    }

    override suspend fun loadComments() {
        delay(500)

        val good = currentGood.value
        if (good != null) {
            val commentsList = workListRepo.loadComments(good)
            comments.value = commentsList
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
        return currentGood.map { it?.additional?.stocks?.toList() }
    }

    override fun getGoodProviders(): LiveData<List<Provider>> {
        return currentGood.map { it?.additional?.providers?.toList() }
    }


    override fun getTaskType(): ITaskType {
        return TaskTypes.WorkList.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}


interface IWorkListTask : ITask {
    suspend fun addGoodByEan(ean: String): Boolean

    suspend fun loadAdditionalGoodInfo()
    suspend fun loadSalesStatistics()
    suspend fun loadDeliveries()
    suspend fun loadComments()

    fun addScanResult(scanResult: ScanResult)

    fun getGoodOptions(): LiveData<GoodOptions>
    fun getGoodStocks(): LiveData<List<Stock>>
    fun getGoodProviders(): LiveData<List<Provider>>
}

// -----------------------------

data class Good(
        val common: CommonGoodInfo,
        var additional: AdditionalGoodInfo? = null,
        val scanResults: MutableLiveData<List<ScanResult>> = MutableLiveData(listOf()),
        var processed: Boolean = false
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

}


data class CommonGoodInfo(
        val ean: String,
        val material: String,
        val matcode: String,
        val name: String,
        val units: Uom,
        var goodGroup: String,
        var purchaseGroup: String,
        var marks: Int = 0,
        val shelfLife: Int,
        val options: GoodOptions
)

data class AdditionalGoodInfo(
        val storagePlaces: String,
        val minStock: Int,
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
        val quantity: Int
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
        val commonPrice: Int,
        val discountPrice: Int
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
        val quantity: Int,
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
        val quantity: Int,
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

}