package com.lenta.movement.models

/** Вид по которому делятся корзины в задании. Находится в настройках задания
 * @see TaskManager.getTaskSettings
 * @see TaskSettings */
enum class GoodsSignOfDivision {
    // Марочные и Партионные остатки
    MARK_PARTS,
    // Алкоголь
    ALCO,
    // Обычный товар
    USUAL,
    // Меркурианский товар
    VET,
    // Партия
    PARTS,
    // Вид товара
    MTART,
    // Еда\Не еда
    FOOD,
    // Поставщик
    LIF_NUMBER,
    // SAP-код товара
    MATERIAL_NUMBER,
    // Секция
    SECTION
}