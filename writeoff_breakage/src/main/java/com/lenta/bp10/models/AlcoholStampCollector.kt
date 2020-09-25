package com.lenta.bp10.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.models.memory.isContainsStamp
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.shared.utilities.extentions.map

class AlcoholStampCollector(private val processExciseAlcoProductService: ProcessExciseAlcoProductService) {

    private val countLiveData: MutableLiveData<Double> = MutableLiveData()
    private val stamps = mutableListOf<TaskExciseStamp>()
    private var preparedStampCode: String = ""

    fun prepare(stampCode: String): Boolean {
        if (containsStamp(code = stampCode)) {
            return false
        }
        this.preparedStampCode = stampCode
        return true
    }


    fun add(materialNumber: String, setMaterialNumber: String, writeOffReason: String, isBadStamp: Boolean): Boolean {
        if (preparedStampCode.isEmpty()) {
            throw UnsupportedOperationException("preparedStampCode was be set before executing this method")
        }

        val stamp = TaskExciseStamp(
                material = materialNumber,
                markNumber = preparedStampCode,
                setMaterialNumber = setMaterialNumber,
                writeOffReason = writeOffReason,
                isBadStamp = isBadStamp
        )

        if (stamps.contains(stamp)) {
            return false
        }

        stamps.add(stamp)
        onDataChanged()
        preparedStampCode = ""

        return true
    }

    fun addBadMark(material: String, writeOffReason: String) {
        val stamp = TaskExciseStamp(
                material = material,
                markNumber = "",
                writeOffReason = writeOffReason,
                isBadStamp = true
        )

        stamps.add(stamp)

        onDataChanged()
    }

    fun rollback() {
        if (stamps.isNotEmpty()) {
            stamps.removeAt(stamps.size - 1)
            onDataChanged()
        }
    }

    fun processAll(reason: WriteOffReason) {
        stamps.forEach { processExciseAlcoProductService.add(reason, 1.0, it) }
        clear()
    }

    fun processAllForSet(reason: WriteOffReason, count: Double) {
        processExciseAlcoProductService.add(
                reason = reason,
                count = count,
                stamp = null
        )
        stamps.forEach { processExciseAlcoProductService.addStamp(reason = reason, stamp = it) }
        clear()
    }

    fun containsStamp(code: String): Boolean {
        return stamps.firstOrNull { it.code == code } != null ||
                processExciseAlcoProductService.taskRepository.getExciseStamps().isContainsStamp(code)
    }

    fun clear() {
        stamps.clear()
        onDataChanged()
    }

    private fun onDataChanged() {
        countLiveData.postValue(stamps.size.toDouble())
    }

    fun observeCount(): LiveData<Double> {
        return countLiveData.map { it }
    }

    fun isNotEmpty(): Boolean {
        return countLiveData.value?: 0.0 > 0.0
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

    fun moveStampsFrom(anotherAlcoholStampCollector: AlcoholStampCollector?) {
        anotherAlcoholStampCollector?.stamps?.let {
            stamps.addAll(anotherAlcoholStampCollector.stamps)
            anotherAlcoholStampCollector.clear()
        }
    }

    fun getPreparedStampCode(): String? {
        return preparedStampCode
    }

}


