package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.Block
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.request.pojo.ProviderInfo

data class TaskOpen(
        val number: String,
        val name: String,
        val type: TaskType?,
        val block: Block,

        val storage: String,
        val control: ControlType,
        val provider: ProviderInfo,
        val reason: ReturnReason?,
        var comment: String,
        var section: String,
        val goodType: String,
        var purchaseGroup: String,
        var goodGroup: String,

        val numberOfGoods: Int,
        val goods: MutableList<GoodOpen> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf(),

        val isStrict: Boolean,
        var isFinished: Boolean
) {

    fun getProviderCodeWithName(): String {
        with(provider){
            return if (code.isNotEmpty() || name.isNotEmpty()) {
                "${code.dropWhile { it == '0' }} $name"
            } else ""
        }
    }

    fun isExistProcessedGood(): Boolean {
        return goods.any { it.isCounted || it.isDeleted }
    }

    fun isExistUncountedGood(): Boolean {
        return goods.any { !it.isCounted && !it.isDeleted }
    }

    fun getFormattedName(withFullName: Boolean = false): String {
        var formattedName = name
        if (!withFullName) {
            runCatching {
                formattedName = name.split(" ")[2]
            }
        }

        return "${type?.code}-$number // $formattedName"
    }

}