package com.lenta.bp14.models.check_list

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lenta.bp14.di.CheckListScope
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.check_list.repo.ICheckListRepo
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.bp14.requests.check_list.CheckListReport
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.sumWith
import javax.inject.Inject

@CheckListScope
class CheckListTask @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val checkListRepo: ICheckListRepo,
        private val taskDescription: CheckListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson,
        private val soundPlayer: ISoundPlayer,
        private val vibrateHelper: IVibrateHelper
) : ICheckListTask {

    override val goods = MutableLiveData<List<Good>>(listOf())

    override fun addGood(good: Good) {
        val goodsList = goods.value!!.toMutableList()
        goodsList.find {
            it.ean == good.ean && it.material == good.material
        }?.let { existGood ->
            val existQuantity = existGood.quantity.value!!.toDoubleOrNull()
            val quantity = good.quantity.value!!.toDoubleOrNull()
            goodsList[goodsList.indexOf(existGood)].quantity.value = existQuantity.sumWith(quantity).dropZeros()
        } ?: goodsList.add(0, good)

        goods.value = goodsList
    }

    override fun deleteSelectedGoods(indices: MutableSet<Int>) {
        val goodsList = goods.value!!.toMutableList()
        val goodsForDelete = mutableListOf<Good>()
        goodsList.mapIndexed { index, good ->
            if (indices.contains(index)) goodsForDelete.add(good)
        }

        goodsList.removeAll(goodsForDelete)
        goods.value = goodsList
    }

    override suspend fun getGoodByMaterial(material: String): Good? {
        return checkListRepo.getGoodByMaterial(material)
    }

    override suspend fun getGoodByEan(ean: String): Good? {
        return checkListRepo.getGoodByEan(ean)
    }

    override fun getTaskType(): ITaskTypeInfo {
        return generalRepo.getTasksTypeInfo(AppTaskTypes.CheckList.taskType)!!
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    override fun getReportData(ip: String): CheckListReport {
        return CheckListReport(
                ip = ip,
                description = taskDescription,
                isNotFinish = false,
                checksResults = goods.value ?: emptyList()
        )
    }

    override suspend fun getGoodRequestResult(ean: String): GoodRequestResult {
        return checkListRepo.getGoodByEan(ean)?.let { good ->
            addGood(good)
            soundPlayer.playBeep()
            vibrateHelper.shortVibrate()
            GoodRequestResult(good = good)
        } ?: GoodRequestResult(good = null)
    }

    override fun isEmpty(): Boolean {
        return goods.value.isNullOrEmpty()
    }

}

interface ICheckListTask : ITask {
    val goods: MutableLiveData<List<Good>>

    suspend fun getGoodByEan(ean: String): Good?
    suspend fun getGoodByMaterial(material: String): Good?

    fun addGood(good: Good)
    fun deleteSelectedGoods(indices: MutableSet<Int>)
    fun getReportData(ip: String): CheckListReport
    suspend fun getGoodRequestResult(ean: String): GoodRequestResult
}

// --------------------------

data class Good(
        val ean: String? = null,
        val material: String,
        val name: String,
        val units: Uom,
        val quantity: MutableLiveData<String>
) {

    fun getFormattedMaterialWithName(): String {
        return material.takeLast(6) + " " + name
    }

}

data class GoodRequestResult(
        val good: Good?
)

