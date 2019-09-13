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

    override fun getGoodInfoByMaterial(material: String): GoodInfo? {
        return checkListRepo.getGoodInfoByMaterial(material)
    }

    override fun getGoodInfoByEan(ean: String): GoodInfo? {
        return checkListRepo.getGoodInfoByEan(ean)
    }

    override fun getGoodInfoByMatcode(matcode: String): GoodInfo? {
        return checkListRepo.getGoodInfoByMatcode(matcode)
    }

    override fun getTaskType(): ITaskType {
        return TaskTypes.CheckPrice.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}

interface ICheckListTask : ITask {
    fun getGoodInfoByMaterial(material: String): GoodInfo?
    fun getGoodInfoByEan(ean: String): GoodInfo?
    fun getGoodInfoByMatcode(matcode: String): GoodInfo?
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
    var number: Int
    val ean: String?
    val material: String?
    val name: String?
    val uom: Uom
    var quantity: MutableLiveData<String>
}

data class Good(
        override var number: Int,
        override val ean: String?,
        override val material: String?,
        override val name: String?,
        override val uom: Uom = Uom.DEFAULT,
        override var quantity: MutableLiveData<String> = MutableLiveData("1")
) : IGood {

    fun getFormattedMaterial(): String? {
        return material?.takeLast(6)
    }

    fun getFormattedMaterialWithName(): String? {
        return getFormattedMaterial() + " " + name
    }

}

