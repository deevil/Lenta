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
    EXCISE,
    MARK,
    VET;

    fun toControlType(): ControlType {
        return when (this) {
            COMMON -> ControlType.COMMON
            ALCOHOL -> ControlType.ALCOHOL
            EXCISE -> ControlType.ALCOHOL
            MARK -> ControlType.MARK
            VET -> ControlType.VET
        }
    }
}

enum class ControlType(val code: String, val description: String) {
    UNKNOWN("", "Неизвестный"),
    COMMON("N", "Обычный"),
    ALCOHOL("A", "Алкоголь"),
    MARK("M", "Маркированный"),
    VET("V", "Животного происхождения");

    fun codeInRus(): String {
        return when (this) {
            COMMON -> "О"
            ALCOHOL -> "А"
            MARK -> "М"
            else -> ""
        }
    }

    fun isCommon(): Boolean {
        return this == ControlType.COMMON
    }

    fun isAlcohol(): Boolean {
        return this == ControlType.ALCOHOL
    }

    fun isMark(): Boolean {
        return this == ControlType.MARK
    }

    fun isVet(): Boolean {
        return this == ControlType.VET
    }

    companion object {
        fun from(code: String): ControlType {
            return when (code) {
                "N" -> COMMON
                "A" -> ALCOHOL
                "M" -> MARK
                "V" -> VET
                else -> UNKNOWN
            }
        }
    }
}

enum class TypeCode(val code: String, val description: String){
    COMMON("ВБП", "Возврат брака прямому поставщику"),
    WHOLESALE("ПКО", "Продажа крупным оптом"),
    NOT_FOOD("ПНО", "Обратная продажа Non Food"),
    FOOD("СПНО", "Обратная продажа Food")
}

enum class CategoryType(val description: String){
    MARK("Марочно"),
    PART("Партионно")
}

enum class ExciseMarkStatus(val code: String){
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

enum class ScreenStatus(val description: String) {
    DEFAULT("Состояние до сканирование первого номера"),
    COMMON("Обычный товар"),
    ALCOHOL("Алкогольный немаркированный товар"),
    EXCISE("Акцизный алкоголь"),
    MARK_150("Марка 150 символов"),
    MARK_68("Марка 68 символов"),
    PART("Партия"),
    BOX("Коробка"),
    MARK("Марка"),
    VET("Животного происхождения")
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

enum class MarkStatus{
    GOOD_MARK,
    BAD_MARK,
    GOOD_CARTON,
    BAD_CARTON,
    GOOD_BOX,
    BAD_BOX,
    UNKNOWN
}

enum class MarkScreenStatus {
    OK,
    OK_BUT_NEED_TO_SCAN_MARK,
    CARTON_ALREADY_SCANNED,
    MARK_ALREADY_SCANNED,
    BOX_ALREADY_SCANNED,
    SOME_MARKS_FROM_BOX_ALREADY_SCANNED,
    FAILURE,
    INCORRECT_EAN_FORMAT,
    GOOD_CANNOT_BE_ADDED,
    INTERNAL_ERROR,
    CANT_SCAN_PACK,
    GOOD_IS_MISSING_IN_TASK,
    MRC_NOT_SAME,
    MRC_NOT_SAME_IN_BASKET,
    NOT_MARKED_GOOD,
    NO_MARKTYPE_IN_SETTINGS,
    NOT_SAME_GOOD,
    ENTER_MRC_FROM_BOX,
    MARKS_MORE_THAN_PLANNED
}

enum class WorkType {
    CREATE,
    OPEN
}