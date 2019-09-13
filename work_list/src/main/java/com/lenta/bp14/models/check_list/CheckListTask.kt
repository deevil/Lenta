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

class CheckListTask(
        private val checkListRepo: ICheckListRepo,
        private val taskDescription: CheckListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ICheckListTask {

    override val goods = MutableLiveData<List<Good>>(listOf())

    override fun getGoodInfoByMaterial(material: String): GoodInfo? {
        return checkListRepo.getGoodInfoByMaterial(material)
    }

    override fun addGood(goodInfo: GoodInfo) {
        val goodsList = goods.value



        val good = goods.find { it.ean == goodInfo.ean }
        if (good != null) {
            val index = goods.indexOf(good)

        }


        goods.add(0, Good(
                number = goods.lastIndex + 2,
                ean = goodInfo.ean,
                material = goodInfo.material,
                name = goodInfo.name + " ${goods.lastIndex + 2}"
        ))
    }

    override fun getTaskType(): ITaskType {
        return TaskTypes.CheckPrice.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}

interface ICheckListTask : ITask {
    val goods: MutableLiveData<List<Good>>

    fun getGoodInfoByMaterial(material: String): GoodInfo?
    fun addGood(goodInfo: GoodInfo)
}

// --------------------------

interface IGoodInfo {
    val ean: String?
    val material: String?
    val name: String?
    val uom: Uom
}

data class GoodInfo(
        override val ean: String?,
        override val material: String?,
        override val name: String?,
        override val uom: Uom
) : IGoodInfo

// --------------------------

interface IGood {
    val number: Int
    val ean: String?
    val material: String?
    val name: String?
    val uom: Uom
    val quantity: MutableLiveData<String>
}

data class Good(
        override val number: Int,
        override val ean: String?,
        override val material: String?,
        override val name: String?,
        override val uom: Uom = Uom.DEFAULT,
        override val quantity: MutableLiveData<String> = MutableLiveData("1")
) : IGood {

    fun getFormattedMaterial(): String? {
        return material?.takeLast(6)
    }

    fun getFormattedMaterialWithName(): String? {
        return getFormattedMaterial() + " " + name
    }

}

