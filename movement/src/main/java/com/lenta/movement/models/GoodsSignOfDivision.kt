package com.lenta.movement.models

import com.lenta.movement.R

/** Вид по которому делятся корзины в задании. Находится в настройках задания
 *  @see TaskManager.getTaskSettings
 *  @see TaskSettings
 *
 *  @param needStringValue - нужно ли получать от корзины описание поля
 *  */
enum class GoodsSignOfDivision(val needStringValue: Boolean) {
    // Марочные и Партионные остатки
    MARK_PARTS(false),

    // Алкоголь
    ALCO(false),

    // Обычный товар
    USUAL(false),

    // Меркурианский товар
    VET(false),

    // Партия
    PARTS(true),

    // Вид товара
    MTART(false),

    // Еда\Не еда
    FOOD(false),

    // Поставщик
    LIF_NUMBER(true),

    // SAP-код товара
    MATERIAL_NUMBER(true),

    // Секция
    SECTION(true)
}

/**
 * Получение наименования категории из string.xml
 */
fun GoodsSignOfDivision.toDescriptionResId() = when (this) {
    GoodsSignOfDivision.MARK_PARTS -> R.string.marked_good
    GoodsSignOfDivision.ALCO -> R.string.alco_good
    GoodsSignOfDivision.USUAL -> R.string.usual_good
    GoodsSignOfDivision.VET -> R.string.vet_good
    GoodsSignOfDivision.PARTS -> R.string.good_parts
    GoodsSignOfDivision.FOOD -> R.string.food
    GoodsSignOfDivision.LIF_NUMBER -> R.string.lif_number
    GoodsSignOfDivision.MATERIAL_NUMBER -> R.string.material_number
    GoodsSignOfDivision.SECTION -> R.string.section
    GoodsSignOfDivision.MTART -> R.string.mtart
}

fun GoodsSignOfDivision.isGisControl() =
        this == GoodsSignOfDivision.MARK_PARTS ||
                this == GoodsSignOfDivision.ALCO ||
                this == GoodsSignOfDivision.USUAL ||
                this == GoodsSignOfDivision.VET