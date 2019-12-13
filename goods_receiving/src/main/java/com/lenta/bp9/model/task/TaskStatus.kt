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
    SentToGIS("15"), //Отправлено в ГИС
    RejectedByGIS("16"), //Отклонено ГИС
    Departure("19"), //Убытие
    Breaking("26"), //Взлом
    AllowedByGIS("37"), //Разрешено ГИС
    Completed("90"), //Выполнено
    TemporaryRejected("98"); //Временно отказано

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
            SentToGIS -> "Отправалено в ГИС"
            RejectedByGIS -> "Отклонено ГИС"
            AllowedByGIS -> "Разрешено ГИС"
            Departure -> "Убытие"
            Breaking -> "Взлом"
            Completed -> "Выполнено"
            TemporaryRejected -> "Временно отказано"
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
                "15" -> SentToGIS
                "16" -> RejectedByGIS
                "19" -> Departure
                "26" -> Breaking
                "37" -> AllowedByGIS
                "90" -> Completed
                "98" -> TemporaryRejected
                else -> Other
            }
        }
    }
}