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

    override suspend fun addGoodByEan(ean: String): Boolean {
        delay(500)

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

            return true
        }

        return false
    }

    suspend fun loadAdditionalGoodInfo() {
        val good = currentGood.value
        if (good != null) {
            val additionalGoodInfo = workListRepo.loadAdditionalGoodInfo(good)
            good.additional = additionalGoodInfo
            currentGood.value = good
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

    fun getGoodOptions(): LiveData<GoodOptions> {
        return currentGood.map { it?.common?.options }
    }

    fun getGoodStocks(): LiveData<List<Stock>> {
        return currentGood.map { it?.additional?.stocks?.toList() }
    }

    fun getGoodProviders(): LiveData<List<Provider>> {
        return currentGood.map { it?.additional?.providers?.toList() }
    }


    override fun getTaskType(): ITaskType {
        return TaskTypes.CheckPrice.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}


interface IWorkListTask : ITask {
    suspend fun addGoodByEan(ean: String): Boolean
}


data class Good(
        val common: CommonGoodInfo,
        var additional: AdditionalGoodInfo? = null,
        var processed: Boolean = false
) {

    fun getFormattedMaterialWithName(): String {
        return "${common.material.takeLast(6)} ${common.name}"
    }

    fun getQuantityWithUnit(): String {
        return "${common.quantity} ${common.unit.name.toLowerCase(Locale.getDefault())}"
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