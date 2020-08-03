package com.lenta.movement.requests.network.models

import com.lenta.movement.models.*
import com.lenta.movement.requests.network.models.startConsolidation.StartConsolidationProcessingUnit
import com.lenta.movement.requests.network.models.startConsolidation.StartConsolidationTaskComposition
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getSapDate
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.orIfNull
import java.util.*

fun Taskable.toTask(): Task {
    val currentStatus = when (currentStatusCode) {
        Task.Status.PUBLISHED_CODE -> Task.Status.Published(currentStatusText)
        Task.Status.COUNTED_CODE -> Task.Status.Counted(currentStatusText)
        Task.Status.TO_CONSOLIDATION_CODE -> Task.Status.ToConsolidation(currentStatusText)
        Task.Status.CONSOLIDATED_CODE -> Task.Status.Consolidated(currentStatusText)
        Task.Status.PROCESSING_ON_GZ_CODE -> Task.Status.ProcessingOnGz(currentStatusText)
        else -> Task.Status.Unknown(currentStatusText.orEmpty())
    }
    val shipmentDate = dateShip?.getSapDate(Constants.DATE_FORMAT_yyyy_mm_dd)
            .orIfNull {
                Logg.e { "Dateship null" }
                Date()
            }
    return Task(
            number = taskNumber.orEmpty(),
            isCreated = notFinish.isSapTrue().not(),
            currentStatus = currentStatus,
            nextStatus = Task.Status.Unknown(nextStatusText.orEmpty()),
            name = description.orEmpty(),
            comment = taskComment.orEmpty(),
            taskType = taskType ?: TaskType.UnknownTaskType,
            movementType = movementType ?: MovementType.UnknownMovementType,
            receiver = werksDstntnt.orEmpty(),
            pikingStorage = lgortSrc.orEmpty(), // Склад комплектации
            shipmentStorage = lgortTarget.orEmpty(), // Склад отгрузки
            shipmentDate = shipmentDate,
            blockType = blockType.orEmpty(),
            quantity = quantityPosition.orEmpty(),
            isNotFinish = notFinish.isSapTrue(),
            isCons = isCons.isSapTrue()
    )
}

fun List<Taskable>.toTaskList(): List<Task> {
    return this.map {
        it.toTask()
    }
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
            .mapValues {
                it.value.map { cargoUnit ->
                    cargoUnit.processingUnitNumber
                }
            }
            .map { map ->
                CargoUnit(
                        number = map.key,
                        eoList = map.value
                                .mapNotNullTo(mutableListOf()) { processingUnitNumber ->
                                    processingUnitNumber
                                            ?.takeIf {
                                                it.isNotEmpty()
                                            }
                                            ?.let {
                                                ProcessingUnit(
                                                        processingUnitNumber = it,
                                                        state = ProcessingUnit.State.COMBINED)
                                            }
                                }
                )
            }.toMutableList()
}