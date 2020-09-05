package com.lenta.bp10.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.models.memory.isContainsBox
import com.lenta.bp10.models.memory.isContainsStamp
import com.lenta.bp10.models.task.ProcessMarkProductService
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.requests.network.pojo.MarkInfo
import com.lenta.shared.utilities.extentions.map

class MarkedGoodStampCollector(private val processMarkProductService: ProcessMarkProductService) {

    private val countLiveData: MutableLiveData<Double> = MutableLiveData()

    private val stamps = mutableListOf<TaskExciseStamp>()

    private val boxes = mutableListOf<String>()

    //private var preparedStampCode: String = ""

    private val lastAddedMarks = mutableListOf<String>()

    /*fun prepare(stampCode: String): Boolean {
        if (isContainsStamp(code = stampCode)) {
            return false
        }
        this.preparedStampCode = stampCode
        return true
    }*/


    fun addMark(markNumber: String, material: String, writeOffReason: String) {
        /*if (preparedStampCode.isEmpty()) {
            throw UnsupportedOperationException("preparedStampCode was be set before executing this method")
        }*/

        lastAddedMarks.clear()

        val stamp = TaskExciseStamp(
                material = material,
                markNumber = markNumber,
                writeOffReason = writeOffReason
        )

        /*if (stamps.contains(stamp)) {
            return false
        }*/

        stamps.add(stamp)
        lastAddedMarks.add(markNumber)

        onDataChanged()
        //preparedStampCode = ""

        //return true
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
            lastAddedMarks.add(markNumber)
        }

        onDataChanged()
    }

    fun rollback() {
        /*if (stamps.isNotEmpty()) {
            stamps.removeAt(stamps.size - 1)
            onDataChanged()
        }*/

        if (lastAddedMarks.isNotEmpty()) {
            lastAddedMarks.forEach { markNumber ->
                val foundMark = stamps.find { it.code == markNumber }

                stamps.remove(foundMark)
                boxes.remove(foundMark?.boxNumber)
            }

            onDataChanged()
        }

    }


    fun processAll(reason: WriteOffReason) {
        stamps.forEach { processMarkProductService.add(reason, 1.0, it) }
        clear()
    }

    fun processAllForSet(reason: WriteOffReason, count: Double) {
        processMarkProductService.add(
                reason = reason,
                count = count,
                stamp = null
        )
        stamps.forEach { processMarkProductService.addStamp(reason = reason, stamp = it) }
        clear()
    }

    fun isContainsStamp(code: String): Boolean {
        return stamps.firstOrNull { it.code == code } != null ||
                processMarkProductService.taskRepository.getExciseStamps().isContainsStamp(code)
    }

    fun isContainsBox(code: String): Boolean {
        return boxes.firstOrNull { it == code } != null ||
                processMarkProductService.taskRepository.getExciseStamps().isContainsBox(code)
    }

    fun clear() {
        stamps.clear()
        boxes.clear()
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

    /*fun getPreparedStampCode(): String? {
        return preparedStampCode
    }*/


}


