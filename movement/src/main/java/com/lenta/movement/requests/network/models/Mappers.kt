package com.lenta.movement.requests.network.models

import com.lenta.movement.models.Task
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.extentions.getSapDate
import com.lenta.shared.utilities.extentions.isSapTrue

fun DbTaskListItem.toTask(): Task {
    return Task(
        number = taskNumber,
        isCreated = notFinish.isSapTrue().not(),
        currentStatus = when (currentStatusCode) {
            "04" -> Task.Status.Published(currentStatusText)
            "10" -> Task.Status.Counted(currentStatusText)
            "13" -> Task.Status.ToConsolidation(currentStatusText)
            "19" -> Task.Status.Consolidated(currentStatusText)
            else -> Task.Status.Unknown(currentStatusText)
        },
        nextStatus = Task.Status.Unknown(nextStatusText),
        name = description,
        comment = taskComment,
        taskType = taskType,
        movementType = movementType,
        receiver = werksDstntnt,
        pikingStorage = lgortSrc,
        shipmentStorage = lgortTarget,
        shipmentDate = dateShip.getSapDate(Constants.DATE_FORMAT_yyyy_mm_dd) ?: error("shipment date parse error (raw date: $dateShip)")
    )
}