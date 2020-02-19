package com.lenta.bp9.model.task

enum class TaskStatus(val taskStatusString: String) {
    Other(""),
    /** Заказано*/
    Ordered("02"), //Заказано
    /** В пути*/
    Traveling("03"), //В пути
    /** Прибыло*/
    Arrived("04"), //Прибыло
    /** Сверяется*/
    Checking("05"), //Сверяется
    /** Сверено*/
    Checked("06"), //Сверено
    /** Разгружается*/
    Unloading("07"), //Разгружается
    /** Разгружено*/
    Unloaded("08"), //Разгружено
    /** Пересчитывается*/
    Recounting("09"), //Пересчитывается
    /** Пересчитано*/
    Recounted("10"), //Пересчитано
    /** Проведено*/
    Booked("11"), //Проведено
    /** Передача (в секцию)*/
    TransferringToSection("12"), //Передача (в секцию)
    /** Передано (в секцию)*/
    TransferredToSection("13"), //Передано (в секцию)
    /** Передано БТО*/
    SentBTO("14"), // Передано БТО
    /** Отправлено в ГИС*/
    SentToGIS("15"), //Отправлено в ГИС
    /** Отклонено ГИС*/
    RejectedByGIS("16"), //Отклонено ГИС
    /** Выбраковка*/
    Breakage("17"), //Выбраковка (Заявление брака ТР)
    /** Заявление брака РЦ*/
    BreakageDeclaringRC("18"), //Заявление брака РЦ
    /** Убытие*/
    Departure("19"), //Убытие
    /** Запрос документов*/
    DocRequest("20"), //Запрос документов
    /** Заявлено*/
    Declared("21"), //Заявлено
    /** В работе*/
    InProcess("22"), //В работе
    /** На исправлении*/
    Editing("23"), //На исправлении
    /** Утвержденно*/
    Comfirmed("24"),//Утвержденно
    /** Компенсировано*/
    Compensated("25"), //Компенсировано
    /** Взлом*/
    Breaking("26"), //Взлом
    /** Отправлено в ГИС. Отгрузка*/
    ShipmentSentToGis("28"), //Отправлено в ГИС. Отгрузка
    /** Разрешено ГИС. Отгрузка*/
    ShipmentAllowedByGis("29"), //Разрешено ГИС. Отгрузка
    /** Отклонено ГИС. Отгрузка*/
    ShipmentRejectedByGis("30"), //Отклонено ГИС. Отгрузка
    /** Отгружено*/
    Shipped("32"), //Отгружено
    /** Разрешено ГИС*/
    AllowedByGIS("37"), //Разрешено ГИС
    /** Готово к отгрузке*/
    ReadyToShipment("38"), //Готово к отгрузке
    /** Транспорт назначен*/
    TransportAssigned("39"), //Транспорт назначен
    /** Загружается*/
    Loading("40"), //Загружается
    /** Контроль условий*/
    ConditionControl("41"), //Контроль условий
    /** Условия проверены*/
    ConditionsTested("42"), //Условия проверены
    /** Загружено*/
    Loaded("43"), //Загружено
    /** Выполнено*/
    Completed("90"), //Выполнено
    /** Временно отказано*/
    TemporaryRejected("98"), //Временно отказано
    /** Аннулировано*/
    Cancel("99"); //Аннулировано

    fun stringValue(): String {
        return when (this) {
            Other -> "Другой"
            Ordered -> "Заказано"
            Traveling -> "В пути"
            Arrived -> "Прибыло"
            Checking -> "Сверяется"
            Checked -> "Сверено"
            Unloading -> "Разгружается"
            Unloaded -> "Разгружено"
            Recounting -> "Пересчитывается"
            Recounted -> "Пересчитано"
            Booked -> "Проведено"
            TransferringToSection -> "Передача (в секцию)"
            TransferredToSection -> "Передано (в секцию)"
            SentBTO -> "Передано БТО"
            SentToGIS -> "Отправалено в ГИС"
            RejectedByGIS -> "Отклонено ГИС"
            Breakage -> "Выбраковка"
            BreakageDeclaringRC -> "Заявление брака РЦ"
            Departure -> "Убытие"
            DocRequest -> "Запрос документов"
            Declared -> "Заявлено"
            InProcess -> "В работе"
            Editing -> "На исправлении"
            Comfirmed -> "Утвержденно"
            Compensated -> "Компенсировано"
            Breaking -> "Взлом"
            ShipmentSentToGis -> "Отправлено в ГИС. Отгрузка"
            ShipmentAllowedByGis -> "Разрешено ГИС. Отгрузка"
            ShipmentRejectedByGis-> "Отклонено ГИС. Отгрузка"
            Shipped -> "Отгружено"
            AllowedByGIS -> "Разрешено ГИС"
            ReadyToShipment -> "Готово к отгрузке"
            TransportAssigned -> "Транспорт назначен"
            Loading -> "Загружается"
            ConditionControl -> "Контроль условий"
            ConditionsTested -> "Условия проверены"
            Loaded -> "Загружено"
            Completed -> "Выполнено"
            TemporaryRejected -> "Временно отказано"
            Cancel -> "Аннулировано"
        }
    }

    companion object {
        fun from(taskStatusString: String): TaskStatus {
            return when(taskStatusString) {
                "02" -> Ordered
                "03" -> Traveling
                "04" -> Arrived
                "05" -> Checking
                "06" -> Checked
                "07" -> Unloading
                "08" -> Unloaded
                "09" -> Recounting
                "10" -> Recounted
                "11" -> Booked
                "12" -> TransferringToSection
                "13" -> TransferredToSection
                "14" -> SentBTO
                "15" -> SentToGIS
                "16" -> RejectedByGIS
                "17" -> Breakage
                "18" -> BreakageDeclaringRC
                "19" -> Departure
                "20" -> DocRequest
                "21" -> Declared
                "22" -> InProcess
                "23" -> Editing
                "24" -> Comfirmed
                "25" -> Compensated
                "26" -> Breaking
                "28" -> ShipmentSentToGis
                "29" -> ShipmentAllowedByGis
                "30" -> ShipmentRejectedByGis
                "32" -> Shipped
                "37" -> AllowedByGIS
                "38" -> ReadyToShipment
                "39" -> TransportAssigned
                "40" -> Loading
                "41" -> ConditionControl
                "42" -> ConditionsTested
                "43" -> Loaded
                "90" -> Completed
                "98" -> TemporaryRejected
                "99" -> Cancel
                else -> Other
            }
        }
    }
}