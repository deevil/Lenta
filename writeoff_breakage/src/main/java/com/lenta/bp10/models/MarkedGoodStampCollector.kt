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

class MarkedGoodStampCollector(private val processMarkedGoodProductService: ProcessMarkedGoodProductService) {

    private val countLiveData: MutableLiveData<Double> = MutableLiveData()

    private val stamps = mutableListOf<TaskExciseStamp>()

    private val boxes = mutableListOf<String>()

    private val lastAddedMarks = mutableListOf<TaskExciseStamp>()

    fun addMark(markNumber: String, material: String, writeOffReason: String) {
        lastAddedMarks.clear()

        val stamp = TaskExciseStamp(
                material = material,
                markNumber = markNumber,
                writeOffReason = writeOffReason
        )

        stamps.add(stamp)
        lastAddedMarks.add(stamp)

        onDataChanged()
    }

    fun addMarks(marks: List<MarkInfo>, material: String, writeOffReason: String) {
        lastAddedMarks.clear()

        marks.forEach { mark ->
            val boxNumber = mark.boxNumber.orEmpty()
            val markNumber = mark.markNumber.orEmpty()

            val stamp = TaskExciseStamp(
                    material = material,
                    markNumber = markNumber,
                    boxNumber = boxNumber,
                    packNumber = mark.packNumber.orEmpty(),
                    writeOffReason = writeOffReason
            )

            if (!boxes.contains(boxNumber)) {
                boxes.add(boxNumber)
            }

            stamps.add(stamp)
            lastAddedMarks.add(stamp)
        }

        onDataChanged()
    }

    fun addBadMark(material: String, writeOffReason: String) {
        lastAddedMarks.clear()

        val stamp = TaskExciseStamp(
                material = material,
                markNumber = "",
                writeOffReason = writeOffReason,
                isBadStamp = true
        )

        stamps.add(stamp)
        lastAddedMarks.add(stamp)

        onDataChanged()
    }

    fun rollback() {
        if (lastAddedMarks.isNotEmpty()) {
            lastAddedMarks.forEach { mark ->
                val foundMark = stamps.find { it == mark }

                stamps.remove(foundMark)
                boxes.remove(foundMark?.boxNumber)
            }

            onDataChanged()
        }
    }


    fun processAll(reason: WriteOffReason) {
        stamps.forEach { processMarkedGoodProductService.add(reason, 1.0, it) }
        clear()
    }

    fun isContainsStamp(code: String): Boolean {
        return stamps.firstOrNull { it.code == code } != null ||
                processMarkedGoodProductService.taskRepository.getExciseStamps().isContainsStamp(code)
    }

    fun isContainsBox(code: String): Boolean {
        return boxes.firstOrNull { it == code } != null ||
                processMarkedGoodProductService.taskRepository.getExciseStamps().isContainsBox(code)
    }

    fun clear() {
        stamps.clear()
        boxes.clear()
        lastAddedMarks.clear()

        onDataChanged()
    }

    private fun onDataChanged() {
        countLiveData.postValue(stamps.size.toDouble())
    }

    fun observeCount(): LiveData<Double> {
        return countLiveData.map { it }
    }

    fun isNotEmpty(): Boolean {
        return countLiveData.value ?: 0.0 > 0.0
    }

    fun getCount(materialNumber: String): Double {
        return stamps.filter { it.materialNumber == materialNumber }.count().toDouble()
    }

    fun clear(materialNumber: String?) {
        val otherStamps = stamps.filter { it.materialNumber != materialNumber }
        stamps.clear()
        stamps.addAll(otherStamps)
        onDataChanged()
    }

    fun moveStampsFrom(anotherMarkedGoodStampAlcoCollector: MarkedGoodStampCollector?) {
        anotherMarkedGoodStampAlcoCollector?.stamps?.let {
            stamps.addAll(anotherMarkedGoodStampAlcoCollector.stamps)
            anotherMarkedGoodStampAlcoCollector.clear()
        }
    }

}


