package com.lenta.bp9.models.task

enum class TaskStatus(val taskStatusString: String) {
    Other(""),
    Ordered("2"), //Заказано
    Traveling("3"), //В пути
    Arrived("4"), //Прибыло
    Checking("5"), //Сверяется
    Checked("6"), //Сверено
    Unloading("7"), //Разгружается
    Unloaded("8"), //Разгружено
    Recounting("9"), //Пересчитывается
    Recounted("10"), //Пересчитано
    Booked("11"), //Проведено
    TransferringToSection("12"), //Передача (в секцию)
    TransferredToSection("13"), //Передано (в секцию)
    SentToGIS("15"), //Отправлено в ГИС
    RejectedByGIS("16"), //Отклонено ГИС
    AllowedByGIS("37"), //Разрешено ГИС
    Completed("90"), //Выполнено
    TemporaryRejected("98"); //Временно отказано

    companion object {
        fun from(taskStatusString: String): TaskStatus {
            return when(taskStatusString) {
                "2" -> Ordered
                "3" -> Traveling
                "4" -> Arrived
                "5" -> Checking
                "6" -> Checked
                "7" -> Unloading
                "8" -> Unloaded
                "9" -> Recounting
                "10" -> Recounted
                "11" -> Booked
                "12" -> TransferringToSection
                "13" -> TransferredToSection
                "15" -> SentToGIS
                "16" -> RejectedByGIS
                "37" -> AllowedByGIS
                "90" -> Completed
                "98" -> TemporaryRejected
                else -> Other
            }
        }
    }
}