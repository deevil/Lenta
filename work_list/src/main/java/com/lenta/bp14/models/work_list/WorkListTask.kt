package com.lenta.bp14.models.work_list

import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
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
        val additional: AdditionalGoodInfo? = null,
        val providers: MutableList<Provider> = mutableListOf(),
        val stocks: MutableList<Stock> = mutableListOf()
)

data class CommonGoodInfo(
        val ean: String,
        val material: String,
        val matcode: String,
        val name: String,
        val unit: Uom,
        var goodGroup: String,
        var purchaseGroup: String,
        var quantity: Int = 0,
        val shelfLifeDays: Int = 5,
        val options: GoodOptions
)


data class AdditionalGoodInfo(
        val ean: String

)


data class GoodOptions(
        val matrixType: MatrixType,
        val section: String,
        val alcohol: Boolean = false,
        val mark: Boolean = false,
        val healthFood: Boolean = false,
        val novelty: Boolean = false
)

data class Stock(
        val id: Int,
        val storageNumber: String,
        val quantity: Int
)

data class Provider(
        val id: Int,
        val code: String,
        val name: String,
        val kipStart: Date,
        val kipEnd: Date
) {

    fun getKipPeriod(): String {
        return "${kipStart.getFormattedDate()} - ${kipEnd.getFormattedDate()}"
    }

}