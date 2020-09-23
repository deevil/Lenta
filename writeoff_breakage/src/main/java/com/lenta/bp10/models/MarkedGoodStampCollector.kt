package com.lenta.bp10.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.models.memory.isContainsBox
import com.lenta.bp10.models.memory.isContainsStamp
import com.lenta.bp10.models.task.ProcessMarkedGoodProductService
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.requests.network.pojo.MarkInfo
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull

class MarkedGoodStampCollector(private val processMarkedGoodProductService: ProcessMarkedGoodProductService) {

    private val countLiveData = MutableLiveData(0.0)

    private val stamps = mutableListOf<List<TaskExciseStamp>>()

    private val boxes = mutableListOf<String>()

    fun addMark(markNumber: String, material: String, writeOffReason: String) {
        val stamp = TaskExciseStamp(
                material = material,
                markNumber = markNumber,
                writeOffReason = writeOffReason
        )

        stamps.add(listOf(stamp))

        onDataChanged()
    }

    fun addMarks(boxNumber: String, marks: List<MarkInfo>, material: String, writeOffReason: String) {
        val stampList = mutableListOf<TaskExciseStamp>()
        marks.mapTo(stampList) { mark ->
            TaskExciseStamp(
                    material = material,
                    markNumber = mark.markNumber.orEmpty(),
                    boxNumber = boxNumber,
                    packNumber = mark.packNumber.orEmpty(),
                    writeOffReason = writeOffReason
            )
        }

        if (!boxes.contains(boxNumber)) {
            boxes.add(boxNumber)
        }

        stamps.add(stampList)

        onDataChanged()
    }

    fun addBadMark(material: String, writeOffReason: String) {
        val stamp = TaskExciseStamp(
                material = material,
                markNumber = "",
                writeOffReason = writeOffReason,
                isBadStamp = true
        )

        stamps.add(listOf(stamp))

        onDataChanged()
    }

    fun rollback() {
        if (stamps.isNotEmpty()) {
            val boxNumber = stamps[stamps.lastIndex][0].boxNumber
            if (boxNumber.isNotEmpty()) {
                boxes.remove(boxNumber)
            }

            stamps.removeAt(stamps.lastIndex)

            onDataChanged()
        }
    }

    fun processAll(reason: WriteOffReason) {
        stamps.flatten().forEach {
            processMarkedGoodProductService.add(reason, 1.0, it)
        }
        clear()
    }


    fun isContainsStamp(code: String): Boolean {
        return stamps.flatten().firstOrNull { it.code == code } != null ||
                processMarkedGoodProductService.taskRepository.getExciseStamps().isContainsStamp(code)
    }

    fun isContainsBox(code: String): Boolean {
        return boxes.firstOrNull { it == code } != null ||
                processMarkedGoodProductService.taskRepository.getExciseStamps().isContainsBox(code)
    }

    fun clear() {
        stamps.clear()
        boxes.clear()

        onDataChanged()
    }

    fun onDataChanged() {
        countLiveData.value = stamps.flatten().size.toDouble()
    }

    fun observeCount(): LiveData<Double> {
        return countLiveData.map { it }
    }

    fun isNotEmpty(): Boolean {
        return countLiveData.value.orIfNull { 0.0 } > 0.0
    }

    fun getCount(materialNumber: String): Double {
        return stamps.flatten().filter { it.materialNumber == materialNumber }.count().toDouble()
    }

    fun clear(materialNumber: String?) {
        val otherStamps = stamps.flatten().filter { it.materialNumber != materialNumber }

        stamps.clear()
        stamps.addAll(listOf(otherStamps))
        onDataChanged()
    }

    fun moveStampsFrom(anotherMarkedGoodStampAlcoCollector: MarkedGoodStampCollector?) {
        anotherMarkedGoodStampAlcoCollector?.stamps?.let {
            stamps.addAll(anotherMarkedGoodStampAlcoCollector.stamps)
            anotherMarkedGoodStampAlcoCollector.clear()
        }
    }

}