package com.lenta.bp14.models.work_list

import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.work_list.repo.WorkListRepo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor

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
        var processed: Boolean = false,
        val common: CommonGoodInfo,
        val additional: AdditionalGoodInfo? = null
)


data class CommonGoodInfo(
        val ean: String,
        val material: String,
        val matcode: String,
        val name: String,
        val unit: Uom,
        var goodGroup: String,
        var purchaseGroup: String
)


data class AdditionalGoodInfo(
        val ean: String,
        val material: String,
        val matcode: String,
        val name: String,
        val unit: Uom,
        var goodGroup: String,
        var purchaseGroup: String
)