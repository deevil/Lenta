package com.lenta.bp15.repository.net_requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.bp15.model.enum.ShoesMarkType
import com.lenta.bp15.model.pojo.Good
import com.lenta.bp15.model.pojo.Mark
import com.lenta.shared.utilities.extentions.IResultWithRetCodes
import com.lenta.shared.utilities.extentions.isSapTrue

data class TaskContentResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val positions: List<PositionRawInfo>?,
        /** Таблица марок задания */
        @SerializedName("ET_TASK_MARK")
        val marks: List<MarkRawInfo>?,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes {

    fun convertToGoods(): List<Good> {
        return positions?.map { positionRawInfo ->
            Good(
                    material = positionRawInfo.material,
                    planQuantity = (positionRawInfo.planQuantity.toDoubleOrNull() ?: 0.0).toInt(),
                    markType = ShoesMarkType.from(positionRawInfo.markTypeCode),
                    marks = marks?.filter { it.material == positionRawInfo.material }?.map { markRawInfo ->
                        Mark(
                                number = markRawInfo.markNumber,
                                isScan = markRawInfo.isScan.isSapTrue()
                        )
                    } ?: emptyList()
            )
        } ?: emptyList()
    }

}