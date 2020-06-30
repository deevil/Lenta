package com.lenta.movement.platform

import android.content.Context
import com.lenta.movement.models.*
import com.lenta.movement.requests.network.models.startConsolidation.CargoUnit

class Formatter(
    val context: Context
) : IFormatter {

    override fun formatMarketName(market: String): String {
        return "ТК - $market"
    }

    override fun getProductName(product: ProductInfo): String {
        return "${product.getMaterialLastSix()} ${product.description}"
    }

    override fun getTaskTypeNameDescription(taskType: TaskType): String {
        return when (taskType) {
            TaskType.TransferWithOrder -> TRANSFER_WITH_ORDER
            TaskType.TransferWithoutOrder -> TRANSFER_WITHOUT_ORDER
        }
    }

    override fun getTaskStatusName(taskStatus: Task.Status): String {
        if (taskStatus.text != null) return taskStatus.text!!

        return when (taskStatus) {
            is Task.Status.Created -> Task.Status.CREATED
            is Task.Status.Counted -> Task.Status.COUNTED
            is Task.Status.Published -> Task.Status.PUBLISHED
            is Task.Status.Unknown -> Task.Status.UNKNOWN
            is Task.Status.ToConsolidation -> Task.Status.TO_CONSOLIDATION
            is Task.Status.Consolidation -> Task.Status.CONSOLIDATION
            is Task.Status.Consolidated -> Task.Status.CONSOLIDATED

        }
    }

    override fun getMovementTypeNameDescription(movementType: MovementType): String {
        return when (movementType) {
            MovementType.SS -> SS_MOVEMENT
            MovementType.SCDS -> SCDS_MOVEMENT // TODO
            MovementType.SCS -> SCS_MOVEMENT // TODO
            MovementType.SCST -> SCST_MOVEMENT // TODO
        }
    }

    override fun getBasketName(basket: Basket): String {
        return "$BASKET ${String.format("%02d", basket.number)}"
    }

    override fun getBasketDescription(basket: Basket, task: Task, settings: TaskSettings): String {
        if (basket.keys.isEmpty()) return ""

        val descriptionBuilder = StringBuilder()

        val signsOfDiv = settings.signsOfDiv

        if (signsOfDiv.contains(GoodsSignOfDivision.ALCO) && basket.keys.first().isAlco) {
            descriptionBuilder.append("A/")
        } else if (signsOfDiv.contains(GoodsSignOfDivision.VET) && basket.keys.first().isVet) {
            descriptionBuilder.append("B/")
        } else if (signsOfDiv.contains(GoodsSignOfDivision.USUAL) && basket.keys.first().isUsual) {
            descriptionBuilder.append("C/")
        }

        descriptionBuilder.append("${basket.keys.first().materialType}/")

        if (task.isCreated) {
            TODO("добавить 'ПП – <номер задания>/'")
        }

        descriptionBuilder.append("C - ${basket.keys.first().sectionId}/")

        descriptionBuilder.append("${basket.keys.first().ekGroup}/")

        if (signsOfDiv.contains(GoodsSignOfDivision.FOOD)) {
            if (basket.keys.first().isFood) {
                descriptionBuilder.append("F")
            } else {
                descriptionBuilder.append("NF")
            }
        }

        if (signsOfDiv.contains(GoodsSignOfDivision.MATERIAL_NUMBER)) {
            descriptionBuilder.append(basket.keys.first().getMaterialLastSix())
        }

        return descriptionBuilder.toString()
    }

    override fun basketGisControl(basket: Basket): String {
        return when {
            basket.isAlco ?: false -> ALCO
            basket.isExciseAlco ?: false -> EXCISE_ALCO
            basket.isNotExciseAlco ?: false -> NOT_EXCISE_ALCO
            basket.isUsual ?: false -> USUAL
            basket.isVet ?: false -> VET
            basket.isFood ?: false -> FOOD
            else -> ""
        }
    }

    override fun getEOSubtitle(eo: ProcessingUnit) : String {
        val builder = StringBuilder()
        builder.append("№${eo.basketNumber}")
        builder.append(
                when {
                    eo.isAlco -> "/A"
                    eo.isUsual -> "/O"
                    else -> ""
                }
        )
        builder.append(
                when (eo.supplier) {
                    null -> ""
                    else -> "/${eo.supplier}"
                }
        )
        return builder.toString()
    }

    override fun getGETitle(ge: CargoUnit): String {
        val builder = StringBuilder()
        val cargoNum = ge.cargoUnitNumber
        val processingUnit = ge.processingUnitNumber
        if (cargoNum.isNotEmpty()) builder.append(cargoNum).append("/$processingUnit")
        else builder.append(processingUnit)
        return builder.toString()
    }

    companion object {
        private const val SS_MOVEMENT = "Для перемещения на ТК"
        private const val SCDS_MOVEMENT = "Для перемещения на ТК" //TODO
        private const val SCS_MOVEMENT = "Для перемещения на ТК" // TODO
        private const val SCST_MOVEMENT = "Для перемещения на ТК" // TODO

        private const val BASKET = "Корзина"

        private const val ALCO = "Алкоголь"
        private const val EXCISE_ALCO = "Марочные остатки"
        private const val NOT_EXCISE_ALCO = "Партионные остатки"
        private const val USUAL = "Обычный товар"
        private const val VET = "Меркурианский товар"
        private const val FOOD = "Еда"

        private const val TRANSFER_WITH_ORDER = "Трансфер с заказа"
        private const val TRANSFER_WITHOUT_ORDER ="Трансфер без заказа"
    }
}