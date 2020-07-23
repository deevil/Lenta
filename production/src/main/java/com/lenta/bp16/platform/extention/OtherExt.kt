package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.TaskStatus
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.bp16.request.pojo.TaskInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.utilities.extentions.isSapTrue


fun TaskInfo.getTaskStatus(): TaskStatus {
    return when (this.blockType) {
        "1" -> TaskStatus.SELF_LOCK
        "2" -> TaskStatus.LOCK
        else -> {
            when {
                this.isPlay.isSapTrue() -> TaskStatus.STARTED
                else -> TaskStatus.COMMON
            }
        }
    }
}

fun <R : IResultWithRetCodes> Either<Failure, R>.getResult(): Either<Failure, R> {
    return this.rightToLeft(
            fnRtoL = { result ->
                result.retCodes.firstOrNull { retCode ->
                    retCode.retCode == 1
                }?.let { retCode ->
                    return@rightToLeft Failure.SapError(retCode.errorText)
                }
            }
    )
}

interface IResultWithRetCodes {
    val retCodes: List<RetCode>
}