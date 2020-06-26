package com.lenta.movement.platform

import android.content.Context
import com.lenta.movement.models.*

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
            TaskType.TransferWithOrder -> "Трансфер с заказа"
            TaskType.TransferWithoutOrder -> "Трансфер без заказа"
        }
    }

    override fun getTaskStatusName(taskStatus: Task.Status): String {
        if (taskStatus.text != null) return taskStatus.text!!

        return when (taskStatus) {
            is Task.Status.Created -> "Создано"
            is Task.Status.Counted -> "Посчитано"
            is Task.Status.Published -> "Опубликованно"
            is Task.Status.Unknown -> "Неизвестно"
            is Task.Status.ToConsolidation -> "К консолидации"
            is Task.Status.Consolidation -> "Консолидация"
            is Task.Status.Consolidated -> "Консолидировано"

        }
    }

    override fun getMovementTypeNameDescription(movementType: MovementType): String {
        return when (movementType) {
            MovementType.SS -> "Для перемещения на ТК"
            MovementType.SCDS -> "Для перемещения на ТК" // TODO
            MovementType.SCS -> "Для перемещения на ТК" // TODO
            MovementType.SCST -> "Для перемещения на ТК" // TODO
        }
    }

    override fun getBasketName(basket: Basket): String {
        return "Корзина ${String.format("%02d", basket.number)}"
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
            basket.isAlco ?: false -> "Алкоголь"
            basket.isExciseAlco ?: false -> "Марочные остатки"
            basket.isNotExciseAlco ?: false -> "Партионные остатки"
            basket.isUsual ?: false -> "Обычный товар"
            basket.isVet ?: false -> "Меркурианский товар"
            basket.isFood ?: false -> "Еда"
            else -> ""
        }
    }

}