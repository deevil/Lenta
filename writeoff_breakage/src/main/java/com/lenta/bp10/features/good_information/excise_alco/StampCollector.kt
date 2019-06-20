package com.lenta.bp10.features.good_information.excise_alco

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.models.memory.containsStamp
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.shared.utilities.extentions.toStringFormatted
import java.lang.UnsupportedOperationException

class StampCollector(
        private val processExciseAlcoProductService: ProcessExciseAlcoProductService,
        private val countLiveData: MutableLiveData<String>) {

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
                materialNumber = materialNumber,
                code = preparedStampCode,
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

    private fun containsStamp(code: String): Boolean {
        return stamps.firstOrNull { it.code == code } != null ||
                processExciseAlcoProductService.taskRepository.getExciseStamps().containsStamp(code)
    }


    private fun clear() {
        stamps.clear()
        onDataChanged()
    }


    private fun onDataChanged() {
        countLiveData.postValue(stamps.size.toDouble().toStringFormatted())
    }


}


