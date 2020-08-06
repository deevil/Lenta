package com.lenta.bp12.model

enum class BlockType {
    SELF_LOCK,
    LOCK,
    UNLOCK;

    companion object {
        fun from(code: String): BlockType {
            return when (code) {
                "1" -> SELF_LOCK
                "2" -> LOCK
                else -> UNLOCK
            }
        }
    }
}

enum class GoodKind {
    COMMON,
    ALCOHOL,
    MARK,
    EXCISE
}

enum class ControlType(val code: String, val description: String) {
    UNKNOWN("UNKNOWN", "Неизвестный"),
    COMMON("N", "Обычный"),
    ALCOHOL("A", "Алкоголь");

    companion object {
        fun from(code: String): ControlType {
            return when (code) {
                "N" -> COMMON
                "A" -> ALCOHOL
                else -> UNKNOWN
            }
        }
    }
}

enum class CategoryType(val description: String){
    MARK("Марочно"),
    PART("Партионно")
}

enum class MarkStatus(val code: String){
    OK("01"),
    BAD("02"),
    OTHER("03"),
    UNKNOWN("04")
}

enum class BoxStatus(val code: String){
    OK("100"),
    ERROR("101")
}

enum class PartStatus(val code: String){
    FOUND("200"),
    NOT_FOUND("201")
}

enum class ScreenStatus(val description: String){
    DEFAULT("Состояние до сканирование первого номера"),
    COMMON("Обычный товар"),
    ALCOHOL("Алкогольный немаркированный товар"),
    EXCISE("Акцизный алкоголь"),
    MARK_150("Марка 150 символов"),
    MARK_68("Марка 68 символов"),
    PART("Партия"),
    BOX("Коробка"),
    SHOES("Обувь"),
    TOBACCO("Сигареты"),
    TIRES("Шины"),
    PERFUME("Парфюм"),
    CLOTHES("Одежда"),
    PHOTO("Фото"),
    BEER("Пиво"),
    MILK("Молоко"),
    MEDICINE("Медицина"),
    UNKNOWN("")
}

enum class ScanInfoMode(val mode: Int){
    MARK(1),
    BOX(2),
    PART(3)
}

enum class TaskSearchMode(val mode: Int){
    COMMON(1),
    WITH_PARAMS(2)
}

enum class MarkType(val description: String) {
    TOBACCO("Табак"),
    SHOES("Обувь"),
    TIRES("Шины"),
    PERFUME("Парфюм"),
    CLOTHES("Одежда"),
    PHOTO("Фото"),
    BEER("Пиво"),
    MILK("Молоко"),
    MEDICINE("Медицина"),
    UNKNOWN("")
}

fun main() {
    MarkType::class.java.enumConstants
}