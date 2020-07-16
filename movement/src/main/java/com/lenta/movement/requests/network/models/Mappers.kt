package com.lenta.movement.requests.network.models

import com.lenta.movement.models.CargoUnit
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.models.Task
import com.lenta.movement.requests.network.models.startConsolidation.StartConsolidationProcessingUnit
import com.lenta.movement.requests.network.models.startConsolidation.StartConsolidationTaskComposition
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.extentions.getSapDate
import com.lenta.shared.utilities.extentions.isSapTrue

fun Taskable.toTask(): Task {
    return Task(
            number = taskNumber,
            isCreated = notFinish.isSapTrue().not(),
            currentStatus = when (currentStatusCode) {
                Task.Status.PUBLISHED_CODE -> Task.Status.Published(currentStatusText)
                Task.Status.COUNTED_CODE -> Task.Status.Counted(currentStatusText)
                Task.Status.TO_CONSOLIDATION_CODE -> Task.Status.ToConsolidation(currentStatusText)
                Task.Status.CONSOLIDATED_CODE -> Task.Status.Consolidated(currentStatusText)
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
            shipmentDate = dateShip.getSapDate(Constants.DATE_FORMAT_yyyy_mm_dd)
                    ?: error("shipment date parse error (raw date: $dateShip)")
    )
}

fun StartConsolidationProcessingUnit.convertToModel(goods: List<StartConsolidationTaskComposition>?): ProcessingUnit {
    val eoGoods = goods?.filter { it.processingUnitNumber == this.processingUnitNumber }
    return ProcessingUnit(
            processingUnitNumber = processingUnitNumber,
            basketNumber = basketNumber,
            supplier = supplier.ifEmpty { null },
            isAlco = isAlco.isSapTrue(),
            isUsual = isUsual.isSapTrue(),
            quantity = quantity,
            goods = eoGoods
    )
}

fun List<StartConsolidationProcessingUnit>.toModelList(goods: List<StartConsolidationTaskComposition>?): List<ProcessingUnit> {
    return this.map {
        it.convertToModel(goods)
    }
}

fun List<RestCargoUnit>.toModelList(): MutableList<CargoUnit> {
    return this.groupBy { it.cargoUnitNumber }
            .mapValues { it.value.map { it.processingUnitNumber } }
            .map { map ->
                CargoUnit(
                        number = map.key,
                        eoList = map.value
                                .mapNotNullTo(mutableListOf()) { processingUnitNumber ->
                                    processingUnitNumber
                                            ?.takeIf { it.isNotEmpty() }
                                            ?.let {
                                                ProcessingUnit(
                                                        processingUnitNumber = it,
                                                        state = ProcessingUnit.State.COMBINED)
                                            }
                                }
                )
            }.toMutableList()
}