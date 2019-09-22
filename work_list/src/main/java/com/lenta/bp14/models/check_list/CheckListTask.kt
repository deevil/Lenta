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

    override var openToEdit = true

    override fun getGoodByMaterial(material: String): Good? {
        return checkListRepo.getGoodByMaterial(material)
    }

    override fun getGoodByEan(ean: String): Good? {
        return checkListRepo.getGoodByEan(ean)
    }

    override fun getGoodByMatcode(matcode: String): Good? {
        return checkListRepo.getGoodByMatcode(matcode)
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

    fun getGoodByMaterial(material: String): Good?
    fun getGoodByEan(ean: String): Good?
    fun getGoodByMatcode(matcode: String): Good?
    fun saveScannedGoodList(goodsList: List<Good>)
}

// --------------------------

interface IGood {
    var number: Int
    val ean: String
    val material: String
    val name: String
    val uom: Uom
    val quantity: MutableLiveData<String>
}

data class Good(
        override var number: Int = 0,
        override val ean: String,
        override val material: String,
        override val name: String,
        override val uom: Uom,
        override val quantity: MutableLiveData<String> = MutableLiveData("")
) : IGood {

    fun getFormattedMaterial(): String? {
        return material.takeLast(6)
    }

    fun getFormattedMaterialWithName(): String? {
        return getFormattedMaterial() + " " + name
    }

}

