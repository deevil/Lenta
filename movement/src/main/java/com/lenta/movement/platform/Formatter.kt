package com.lenta.movement.platform

import com.lenta.movement.models.*
import com.lenta.shared.models.core.Uom

class Formatter : IFormatter {

    override fun formatMarketName(market: String): String {
        return "$TK - $market"
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
        taskStatus.text?.let {
            return it
        }

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

        return buildString {
            val signsOfDiv = settings.signsOfDiv

            if (signsOfDiv.contains(GoodsSignOfDivision.ALCO) && basket.keys.first().isAlco) {
                append(A_AND_SLASH)
            } else if (signsOfDiv.contains(GoodsSignOfDivision.VET) && basket.keys.first().isVet) {
                append(B_AND_SLASH)
            } else if (signsOfDiv.contains(GoodsSignOfDivision.USUAL) && basket.keys.first().isUsual) {
                append(C_AND_SLASH)
            }

            append("${basket.keys.first().materialType}/")

            if (task.isCreated) {
                TODO("добавить 'ПП – <номер задания>/'")
            }

            append("C - ${basket.keys.first().sectionId}/")

            append("${basket.keys.first().ekGroup}/")

            if (signsOfDiv.contains(GoodsSignOfDivision.FOOD)) {
                if (basket.keys.first().isFood) {
                    append(BASKET_DESC_FOOD_CHAR)
                } else {
                    append(BASKET_DESC_NOT_FOOD_CHAR)
                }
            }

            if (signsOfDiv.contains(GoodsSignOfDivision.MATERIAL_NUMBER)) {
                append(basket.keys.first().getMaterialLastSix())
            }
        }
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

    override fun getEOSubtitle(eo: ProcessingUnit): String {
        val geNumber = eo.cargoUnitNumber
        return if (geNumber == null) {
            getEOSubtitleForInsides(eo)
        } else {
            "$GE-$geNumber"
        }
    }

    override fun getEOSubtitleForInsides(eo: ProcessingUnit): String {
        val isAlco = eo.isAlco ?: false
        val isUsual = eo.isUsual ?: false
        return buildString {
            append("№${eo.basketNumber}")
            append(
                    when {
                        isAlco -> SLASH_AND_ALCO_CHAR
                        isUsual -> SLASH_AND_USUAL_CHAR
                        else -> ""
                    }
            )
            append(eo.supplier.orEmpty())
        }
    }

    override fun getGETitle(ge: CargoUnit): String {
        val eoList = ge.eoList
        return if (ge.eoList.size == 1) {
            eoList[0].processingUnitNumber
        } else {
            ge.number
        }
    }

    override fun getOrderUnitsNameByCode(orderUnits: String): String =
            when (orderUnits) {
                Uom.DEFAULT.code -> Uom.DEFAULT.name
                Uom.G.code -> Uom.G.name
                Uom.KAR.code -> Uom.KAR.name
                Uom.KG.code -> Uom.KG.name
                Uom.ST.code -> Uom.KG.name
                else -> "Wrong uom code"
            }


    companion object {
        private const val A_AND_SLASH = "A/"
        private const val B_AND_SLASH = "B/"
        private const val C_AND_SLASH = "C/"

        private const val TK = "ТК"
        private const val GE = "ГЕ"

        private const val SS_MOVEMENT = "Для перемещения на ТК"
        private const val SCDS_MOVEMENT = "Для перемещения на ТК" //TODO
        private const val SCS_MOVEMENT = "Для перемещения на ТК" // TODO
        private const val SCST_MOVEMENT = "Для перемещения на ТК" // TODO

        private const val BASKET = "Корзина"
        private const val BASKET_DESC_FOOD_CHAR = "F"
        private const val BASKET_DESC_NOT_FOOD_CHAR = "NF"

        private const val ALCO = "Алкоголь"
        private const val SLASH_AND_ALCO_CHAR = "/А"
        private const val EXCISE_ALCO = "Марочные остатки"
        private const val NOT_EXCISE_ALCO = "Партионные остатки"
        private const val USUAL = "Обычный товар"
        private const val SLASH_AND_USUAL_CHAR = "/О"
        private const val VET = "Меркурианский товар"
        private const val FOOD = "Еда"

        private const val TRANSFER_WITH_ORDER = "Трансфер с заказа"
        private const val TRANSFER_WITHOUT_ORDER = "Трансфер без заказа"
    }
}