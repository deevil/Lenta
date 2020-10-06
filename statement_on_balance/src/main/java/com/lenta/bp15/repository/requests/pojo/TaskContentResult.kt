package com.lenta.bp15.repository.requests.pojo

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
                    marks = marks?.map { markRawInfo ->
                        val key = markRawInfo.number
                        val value = Mark(
                                material = markRawInfo.material,
                                number = markRawInfo.number,
                                isScan = markRawInfo.isScan.isSapTrue()
                        )
                        key to value
                    }?.toMap() ?: emptyMap()
            )
        } ?: emptyList()
    }

}