package com.lenta.bp14.models.work_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lenta.bp14.di.WorkListScope
import com.lenta.bp14.models.BaseProductInfo
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.filter.FilterFieldType
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.work_list.repo.IWorkListRepo
import com.lenta.bp14.requests.work_list.WorkListReport
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.extentions.*
import java.util.*
import javax.inject.Inject

@WorkListScope
class WorkListTask @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val workListRepo: IWorkListRepo,
        private val taskDescription: WorkListTaskDescription,
        private val filterableDelegate: IFilterable,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : IWorkListTask, IFilterable by filterableDelegate {

    private val checkResults by lazy {
        taskDescription.taskInfoResult?.checkResults?.toList() ?: emptyList()
    }

    private val marks by lazy {
        taskDescription.taskInfoResult?.marks?.toList() ?: emptyList()
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
                    good.scanResults = checkResults.filter { it.matNr == position.matNr }.map { result ->
                        ScanResult(
                                quantity = result.quantity,
                                comment = result.comment,
                                productionDate = result.producedDate.getDate(Constants.DATE_FORMAT_ddmmyy),
                                expirationDate = result.shelfLife.getDate(Constants.DATE_FORMAT_ddmmyy)
                        )
                    }.toMutableList()
                    good.marks = marks.filter { it.matNr == position.matNr }.map { mark ->
                        mark.markNumber
                    }.toMutableSet()

                    goodsList.add(good)
                }
            }

            goods.value = goodsList
            isLoadedTaskList = true
        }
    }

    override suspend fun addGoodToList(good: Good) {
        goods.value?.find { it.material == good.material }?.let { existGood ->
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

    override suspend fun getGoodByEan(ean: String): Good? {
        return workListRepo.getGoodByEan(ean)
    }

    override fun updateAdditionalGoodInfo(additionalGoodInfo: AdditionalGoodInfo) {
        val good = currentGood.value!!
        good.additional = additionalGoodInfo
        currentGood.value = good
    }

    override fun addScanResult(scanResult: ScanResult) {
        currentGood.value!!.scanResults.add(scanResult)
    }

    override fun getTaskType(): ITaskTypeInfo {
        return generalRepo.getTasksTypeInfo(AppTaskTypes.WorkList.taskType)!!
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    fun deleteScanResultsByComments(comments: List<String>) {
        val good = currentGood.value!!
        good.scanResults.removeAll { comments.contains(it.comment) }
        currentGood.value = good
    }

    fun deleteScanResultsByShelfLives(shelfLives: List<String>) {
        val good = currentGood.value!!
        good.scanResults.removeAll { shelfLives.contains(it.getFormattedProductionDate() + it.getFormattedExpirationDate()) }
        currentGood.value = good
    }

    override fun setCurrentGoodProcessed() {
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
                isNotFinish = isNotAllGoodsProcessed(),
                checksResults = goods.value!!
        )
    }

    private fun isNotAllGoodsProcessed(): Boolean {
        taskDescription.taskInfoResult?.positions?.let { positions ->
            positions.map { it.matNr }.forEach { material ->
                val good = goods.value?.find { it.material == material }
                if (good == null || !good.isProcessed) return true
            }
        }

        return false
    }

    override fun isEmpty(): Boolean {
        return goods.value.isNullOrEmpty()
    }

    override fun isHaveDiscrepancies(): Boolean {
        return getProcessingList().value?.isNotEmpty() == true
    }

    override fun getListOfDifferences(): LiveData<List<BaseProductInfo>> {
        return getProcessingList().map { list ->
            list?.map { item ->
                BaseProductInfo(
                        matNr = item.material,
                        name = item.name
                )
            }
        }
    }

    override fun setMissing(matNrList: List<String>) {
        //TODO implement this
    }

    override fun getProcessingList(): LiveData<List<Good>> {
        return goods.map { list -> list?.filter { !it.isProcessed } }
    }

    override fun getProcessedList(): LiveData<List<Good>> {
        return goods.map { list -> list?.filter { it.isProcessed } }
    }

    override fun deleteSelectedGoods(materials: List<String>) {
        val goodsList = goods.value!!
        materials.forEach { material ->
            goodsList.find { it.material == material }?.let { good ->
                if (isGoodFromTaskList(material)) {
                    good.isProcessed = false
                    good.scanResults.clear()
                } else {
                    goodsList.remove(good)
                }
            }
        }

        goods.value = goodsList
    }

    private fun isGoodFromTaskList(material: String): Boolean {
        return taskDescription.taskInfoResult?.positions?.find { it.matNr == material } != null
    }

    override fun getSearchList(): LiveData<List<Good>> {
        return goods.combineLatest(filterableDelegate.onFiltersChangesLiveData).map {
            requireNotNull(it)
            val goodsList = it.first
            goodsList.filter { good ->
                filter(good)
            }
        }
    }

    private fun filter(good: Good): Boolean {
        filterableDelegate.filtersMap.forEach {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (it.key) {
                FilterFieldType.SECTION -> {
                    if (!good.options.section.contains(it.value.value)) {
                        return false
                    }
                }
                FilterFieldType.GROUP -> {
                    if (!good.goodGroup.contains(it.value.value)) {
                        return false
                    }
                }
                FilterFieldType.PLACE_STORAGE -> {
                    val storagePlaces = good.additional?.storagePlaces ?: ""
                    if (!storagePlaces.contains(it.value.value)) {
                        return false
                    }
                }
                FilterFieldType.COMMENT -> {
                    var existComment = false
                    good.comments.map { comment ->
                        if (comment.contains(it.value.value)) {
                            existComment = true
                            return@map
                        }
                    }

                    if (!existComment) {
                        return false
                    }
                }
            }
        }

        return true
    }

    override fun isMarkAlreadyAdded(markNumber: String): Boolean {
        return currentGood.value?.marks?.contains(markNumber) ?: false
    }

    override fun deleteMark(markNumber: String) {
        val good = currentGood.value!!
        currentGood.value?.marks?.remove(markNumber)
        currentGood.value = good
    }

    override fun addMark(mark: String) {
        val good = currentGood.value!!
        good.marks.add(mark)
        currentGood.value = good
    }

}


interface IWorkListTask : ITask, IFilterable {
    var isLoadedTaskList: Boolean
    val goods: MutableLiveData<MutableList<Good>>
    var currentGood: MutableLiveData<Good>

    suspend fun loadTaskList()
    suspend fun getGoodByMaterial(material: String): Good?
    suspend fun getGoodByEan(ean: String): Good?
    suspend fun addGoodToList(good: Good)

    fun deleteSelectedGoods(materials: List<String>)
    fun addScanResult(scanResult: ScanResult)
    fun setCurrentGoodProcessed()
    fun getProcessingList(): LiveData<List<Good>>
    fun getProcessedList(): LiveData<List<Good>>

    fun getReportData(ip: String): WorkListReport
    fun getSearchList(): LiveData<List<Good>>
    fun updateAdditionalGoodInfo(additionalGoodInfo: AdditionalGoodInfo)
    fun isMarkAlreadyAdded(markNumber: String): Boolean
    fun deleteMark(markNumber: String)
    fun addMark(mark: String)
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
        val shelfLifeType: List<String>, // Не используется, типы сроков предустановлены.
        val comments: List<String>,
        val options: GoodOptions,
        var isProcessed: Boolean = false,

        var additional: AdditionalGoodInfo? = null,
        val sales: MutableLiveData<SalesStatistics> = MutableLiveData(),
        val deliveries: MutableLiveData<List<Delivery>> = MutableLiveData(emptyList()),
        var scanResults: MutableList<ScanResult> = mutableListOf(),
        var marks: MutableSet<String> = mutableSetOf()
) {

    fun getFormattedMaterialWithName(): String {
        return "${material.takeLast(6)} $name"
    }

    fun isNotMarkedGood(): Boolean {
        return options.goodType == GoodType.COMMON || options.goodType == GoodType.ALCOHOL
    }

    fun getEanWithUnits(): String {
        return if (ean != null) "${ean}/${units.name}" else ""
    }

    fun getGroups(): String {
        return "$goodGroup/$purchaseGroup"
    }

    fun getShelfLifeInMills(): Long {
        return shelfLife.toLong() * 24 * 60 * 60 * 1000
    }

    fun getTotalQuantity(): Double {
        var quantity = 0.0
        if (isNotMarkedGood()) {
            scanResults.map { result ->
                quantity = quantity.sumWith(result.quantity)
            }
        } else {
            quantity = quantity.sumWith(marks.size.toDouble())
        }

        return quantity
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
        val minStock: String,
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