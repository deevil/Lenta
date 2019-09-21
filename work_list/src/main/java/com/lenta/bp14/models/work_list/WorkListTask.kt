package com.lenta.bp14.models.work_list

import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.work_list.repo.WorkListRepo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.extentions.getFormattedDate
import java.util.*

class WorkListTask(
        private val workListRepo: WorkListRepo,
        private val taskDescription: WorkListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ITask {

    val processing: MutableList<Good> = mutableListOf()
    val processed: MutableList<Good> = mutableListOf()
    val search: MutableList<Good> = mutableListOf()

    private var currentList = processed

    var currentGood: Good? = null

    fun getGoodByEan(ean: String): Good? {
        val good = currentList.find { it.common.ean == ean }
        if (good != null) {
            return good
        }

        val commonGoodInfo = workListRepo.getCommonGoodInfoByEan(ean)
        if (commonGoodInfo != null) {
            return Good(
                    number = currentList.size + 1,
                    common = commonGoodInfo
            )
        }

        return null
    }

    fun getAdditionalGoodInfo(ean: String): AdditionalGoodInfo? {
        return workListRepo.getAdditionalGoodInfo(ean)
    }

    fun setCurrentList(tabPosition: Int) {
        currentList = when (tabPosition) {
            GoodsListTab.PROCESSED.position -> processed
            GoodsListTab.PROCESSING.position -> processing
            GoodsListTab.SEARCH.position -> search
            else -> processed
        }
    }


    override fun getTaskType(): ITaskType {
        return TaskTypes.CheckPrice.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}


data class Good(
        var number: Int,
        val common: CommonGoodInfo,
        var additional: AdditionalGoodInfo? = null
) {

    fun getFormattedMaterialWithName(): String? {
        return "${common.material.takeLast(6)} ${common.name}"
    }

    fun isCommonGood(): Boolean {
        return common.options.goodType == GoodType.COMMON
    }

    fun getEanWithUnits(): String? {
        return "${common.ean}/${common.unit.name}"
    }

    fun getGoodWithPurchaseGroups(): String? {
        return "${common.goodGroup}/${common.purchaseGroup}"
    }

}


data class CommonGoodInfo(
        val ean: String,
        val material: String,
        val matcode: String,
        val name: String,
        val unit: Uom,
        var goodGroup: String,
        var purchaseGroup: String,
        var quantity: Int = 0,
        var marks: Int = 0,
        val shelfLifeDays: Int = 5,
        val serverComments: MutableList<String>,
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