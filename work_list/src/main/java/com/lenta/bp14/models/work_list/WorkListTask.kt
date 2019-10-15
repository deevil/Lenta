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
import com.lenta.bp14.requests.work_list.WorkListReport
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.extentions.getDate
import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.map
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

    private val checkResults by lazy {
        taskDescription.taskInfoResult?.checkResults?.toList() ?: emptyList()
    }

    override var isLoadedTaskList = false

    override val goods = MutableLiveData<MutableList<Good>>(mutableListOf())

    override var currentGood = MutableLiveData<Good>()

    override suspend fun loadTaskList() {
        taskDescription.taskInfoResult?.positions?.let { positions ->
            val goodsList = mutableListOf<Good>()
            positions.forEach { position ->
                getGoodByMaterial(position.matNr)?.let { good ->
                    good.isProcessed = position.isProcessed.isSapTrue()
                    good.scanResults.value = checkResults.filter { it.matNr == position.matNr }.map { result ->
                        ScanResult(
                                quantity = result.quantity,
                                comment = result.comment,
                                productionDate = result.producedDate.getDate(Constants.DATE_FORMAT_ddmmyy),
                                expirationDate = result.shelfLife.getDate(Constants.DATE_FORMAT_ddmmyy)
                        )
                    }

                    goodsList.add(good)
                }
            }

            goods.value = goodsList
            isLoadedTaskList = true
        }
    }

    override suspend fun addGood(good: Good) {
        goods.value?.find { it.ean == good.ean && it.material == good.material }?.let { existGood ->
            currentGood.value = existGood
            return
        }

        val goodsList = goods.value!!
        goodsList.add(0, good)
        goods.value = goodsList

        currentGood.value = good
    }

    override suspend fun getGoodByMaterial(material: String): Good? {
        return workListRepo.getGoodByMaterial(material)
    }

    override fun addScanResult(scanResult: ScanResult) {
        val scanResultsList = currentGood.value?.scanResults?.value?.toMutableList()
        scanResultsList?.add(scanResult)
        currentGood.value?.scanResults?.value = scanResultsList
    }

    override fun getGoodOptions(): LiveData<GoodOptions> {
        return currentGood.map { it?.options }
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

    override fun setGoodProcessed() {
        val goodsList = goods.value!!

        currentGood.value?.let { good ->
            good.isProcessed = true
            goodsList.removeAll { it.material == good.material }
            goodsList.add(0, good)
        }

        goods.value = goodsList
    }

    override fun getReportData(ip: String): WorkListReport {
        return WorkListReport(
                ip = ip,
                description = taskDescription,
                isNotFinish = false,
                checksResults = goods.value ?: emptyList()
        )
    }

    override fun isEmpty(): Boolean {
        return goods.value.isNullOrEmpty()
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

}


interface IWorkListTask : ITask {
    var isLoadedTaskList: Boolean
    val goods: MutableLiveData<MutableList<Good>>
    var currentGood: MutableLiveData<Good>

    suspend fun loadTaskList()
    suspend fun getGoodByMaterial(material: String): Good?
    suspend fun addGood(good: Good)

    fun addScanResult(scanResult: ScanResult)
    fun setGoodProcessed()

    fun getGoodOptions(): LiveData<GoodOptions>
    fun getGoodStocks(): LiveData<List<Stock>>
    fun getGoodProviders(): LiveData<List<Provider>>

    fun getReportData(ip: String): WorkListReport
}

// -----------------------------

data class Good(
        val ean: String? = null,
        val material: String,
        val name: String,
        val units: Uom,
        val defaultValue: Double = 0.0,
        var goodGroup: String,
        var purchaseGroup: String,
        val shelfLife: Int,
        val remainingShelfLife: Int,
        val shelfLifeType: MutableLiveData<List<String>> = MutableLiveData(emptyList()),
        val comments: MutableLiveData<List<String>> = MutableLiveData(emptyList()),
        val options: GoodOptions,
        var isProcessed: Boolean = false,

        var additional: MutableLiveData<AdditionalGoodInfo> = MutableLiveData(),
        val sales: MutableLiveData<SalesStatistics> = MutableLiveData(),
        val deliveries: MutableLiveData<List<Delivery>> = MutableLiveData(emptyList()),
        val scanResults: MutableLiveData<List<ScanResult>> = MutableLiveData(emptyList())
) {

    fun getFormattedMaterialWithName(): String {
        return "${material.takeLast(6)} $name"
    }

    fun isCommonGood(): Boolean {
        return options.goodType == GoodType.COMMON
    }

    fun getEanWithUnits(): String? {
        return if (ean != null) "${ean}/${getUnits()}" else ""
    }

    fun getGoodWithPurchaseGroups(): String? {
        return "$goodGroup/$purchaseGroup"
    }

    fun getShelfLifeInMills(): Long {
        return (shelfLife * 24 * 60 * 60 * 1000).toLong()
    }

    fun getUnits(): String {
        return units.name.toLowerCase(Locale.getDefault())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Good) return false
        if (ean != other.ean) return false
        if (material != other.material) return false
        return true
    }

    override fun hashCode(): Int {
        var result = ean?.hashCode() ?: 0
        result = 31 * result + material.hashCode()
        return result
    }

}

data class AdditionalGoodInfo(
        val storagePlaces: String,
        val minStock: Double,
        val inventory: String,
        val arrival: String,
        val commonPrice: Double,
        val discountPrice: Double,
        val promoName: String,
        val promoPeriod: String,
        val providers: List<Provider>,
        val stocks: List<Stock>
)

data class GoodOptions(
        val matrixType: MatrixType,
        val section: String,
        val goodType: GoodType,
        val healthFood: Boolean = false,
        val novelty: Boolean = false
)

data class Stock(
        val storage: String,
        val quantity: Double
)

data class Provider(
        val code: String,
        val name: String,
        val period: String
)

// -----------------------------

data class SalesStatistics(
        val lastSaleDate: Date,
        val daySales: Double,
        val weekSales: Double
)

data class Delivery(
        val status: String,
        val type: String,
        val quantity: Double,
        val date: Date
)

// -----------------------------

data class ScanResult(
        val quantity: Double,
        val comment: String,
        val productionDate: Date?,
        val expirationDate: Date?,
        val markNumber: String? = null
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