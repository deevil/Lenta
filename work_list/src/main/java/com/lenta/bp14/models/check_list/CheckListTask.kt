package com.lenta.bp14.models.check_list

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.check_list.repo.ICheckListRepo
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.sumWith

class CheckListTask(
        private val checkListRepo: ICheckListRepo,
        private val taskDescription: CheckListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ICheckListTask {

    override var openToEdit = true

    override val goods = MutableLiveData<List<Good>>(listOf())

    override fun addGood(good: Good) {
        val goodsList = goods.value!!.toMutableList()
        val existGood = goodsList.find { it.ean == good.ean }
        if (existGood != null) {
            val existQuantity = existGood.quantity.value!!.toDoubleOrNull()
            val quantity = good.quantity.value!!.toDoubleOrNull()
            goodsList[goodsList.indexOf(existGood)].quantity.value = existQuantity.sumWith(quantity).dropZeros()
        } else {
            goodsList.add(0, good)
        }

        goods.value = goodsList
    }

    override fun deleteSelectedGoods(indices: MutableSet<Int>) {
        val goodsList = goods.value!!.toMutableList()
        indices.apply {
            val eans = goods.value?.filterIndexed { index, _ ->
                this.contains(index)
            }?.map { it.ean }?.toSet() ?: emptySet()

            goodsList.removeAll { eans.contains(it.ean) }
        }

        goods.value = goodsList
    }

    override fun getGoodByMaterial(material: String): Good? {
        return goods.value?.find { it.material == material } ?: checkListRepo.getGoodByMaterial(material)
    }

    override fun getGoodByEan(ean: String): Good? {
        return goods.value?.find { it.ean == ean } ?: checkListRepo.getGoodByEan(ean)
    }

    override fun getGoodByMatcode(matcode: String): Good? {
        return goods.value?.find { it.ean == matcode } ?: checkListRepo.getGoodByEan(matcode)
    }

    override fun saveScannedGoodList(goodsList: List<Good>) {

    }

    override fun getTaskType(): ITaskType {
        return TaskTypes.CheckList.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}

interface ICheckListTask : ITask {
    var openToEdit: Boolean
    val goods: MutableLiveData<List<Good>>

    fun getGoodByMaterial(material: String): Good?
    fun getGoodByEan(ean: String): Good?
    fun getGoodByMatcode(matcode: String): Good?

    fun addGood(good: Good)
    fun deleteSelectedGoods(indices: MutableSet<Int>)

    fun saveScannedGoodList(goodsList: List<Good>)
}

// --------------------------

data class Good(
        val ean: String,
        val material: String,
        val name: String,
        val units: Uom,
        val quantity: MutableLiveData<String>
) {

    fun getFormattedMaterial(): String {
        return material.takeLast(6)
    }

    fun getFormattedMaterialWithName(): String {
        return getFormattedMaterial() + " " + name
    }

}

