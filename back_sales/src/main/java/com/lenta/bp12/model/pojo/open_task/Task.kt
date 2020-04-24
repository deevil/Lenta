package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.TaskStatus
import com.lenta.bp12.model.pojo.Block
import com.lenta.bp12.model.pojo.Properties
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.utilities.Logg

data class Task(
        val number: String = "",
        val name: String,
        val properties: Properties?,
        val storage: String,
        val reason: ReturnReason,
        val goods: MutableList<Good> = mutableListOf(),
        var isProcessed: Boolean = false,
        val isStrict: Boolean = false,
        var status: TaskStatus = TaskStatus.COMMON,
        val block: Block,
        val control: ControlType = ControlType.UNKNOWN,
        var comment: String = "",
        val provider: ProviderInfo,
        val quantity: Int = 0
) {

    fun getProviderCodeWithName(): String {
        return "${provider.code} ${provider.name}"
    }

    fun updateGood(good: Good?) {
        good?.let { goodUpdate ->
            val index = goods.indexOf(goods.find { it.material == goodUpdate.material })
            if (index >= 0) {
                goods.removeAt(index)
            }

            goods.add(0, goodUpdate)
        }
    }

    fun isExistProcessedPositions(): Boolean {
        return goods.any { good -> good.positions.any { it.isCounted || it.isDelete } }
    }

    fun isExistUncountedPositions(): Boolean {
        return goods.any { good -> good.positions.any { !it.isCounted && !it.isDelete } }
    }

}