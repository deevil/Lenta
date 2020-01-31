package com.lenta.bp9.model.task

enum class TaskStatus(val taskStatusString: String) {
    Other(""),
    Ordered("02"), //Заказано
    Traveling("03"), //В пути
    Arrived("04"), //Прибыло
    Checking("05"), //Сверяется
    Checked("06"), //Сверено
    Unloading("07"), //Разгружается
    Unloaded("08"), //Разгружено
    Recounting("09"), //Пересчитывается
    Recounted("10"), //Пересчитано
    Booked("11"), //Проведено
    TransferringToSection("12"), //Передача (в секцию)
    TransferredToSection("13"), //Передано (в секцию)
    SentBTO("14"), // Передано БТО
    SentToGIS("15"), //Отправлено в ГИС
    RejectedByGIS("16"), //Отклонено ГИС
    Breakage("17"), //Выбраковка
    BreakageDeclaringRC("18"), //Заявление брака РЦ
    Departure("19"), //Убытие
    DocRequest("20"), //Запрос документов
    Declared("21"), //Заявлено
    InProcess("22"), //В работе
    Editing("23"), //На исправлении
    Comfirmed("24"),//Утвержденно
    Compensated("25"), //Компенсировано
    Breaking("26"), //Взлом
    ShipmentSentToGis("28"), //Отправлено в ГИС. Отгрузка
    ShipmentAllowedByGis("29"), //Разрешено ГИС. Отгрузка
    ShipmentRejectedByGis("30"), //Отклонено ГИС. Отгрузка
    Shipped("32"), //Отгружено
    AllowedByGIS("37"), //Разрешено ГИС
    ReadyToShipment("38"), //Готово к отгрузке
    TransportAssigned("39"), //Транспорт назначен
    Loading("40"), //Загружается
    ConditionControl("41"), //Контроль условий
    ConditionsTested("42"), //Условия проверены
    Loaded("43"), //Загружено
    Completed("90"), //Выполнено
    TemporaryRejected("98"), //Временно отказано
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