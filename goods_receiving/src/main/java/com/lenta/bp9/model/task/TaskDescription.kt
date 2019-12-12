package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.date_time.DateTimeUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//ТП. Таблица 75 ZSGRZ_TASK_EXCH Структура карточки задания
data class TaskDescription(val currentStatus: TaskStatus,   //Код текущего статуса (CUR_STAT)
                      val currentStatusText: String,   //Текс текущего статуса (CUR_ST_TEXT)
                      var currentStatusDate: String,   //Дата текущего статуса (CUR_ST_DATE)
                      var currentStatusTime: String,   //Время текущего статуса (CUR_ST_TIME)
                      val nextStatusText: String,      //Следующий статус (NEXT_ST_TEXT)
                      var nextStatusDate: String,      //Дата следуюшего статуса, изначальна равна текущей дате, может быть изменена пользователем
                      var nextStatusTime: String,      //Время следуюшего статуса, изначальна равно текущму времени, может быть изменено пользователем
                      val ttnNumber: String,           //Номер ТН\ТТН (ZTTN)
                      val orderNumber: String,         //Номер заказа (EBELN)
                      val deliveryNumber: String,      //Номер поставки\Транспортировки (VBELN)
                      val plannedDeliveryDate: String, //Плановая дата поставки (DATE_PLAN)
                      val plannedDeliveryTime: String, //Плановое время поставки (TIME_PLAN)
                      val actualArrivalDate: String,   //Фактическая дата прибытия (DATE_FACT)
                      val actualArrivalTime: String,   //Фактическое время прибытие (TIME_FACT)
                      val quantityPositions: Int,   //Количество позиций (QNT_POS)
                      val isOverdue: Boolean,          //Индикатор: Просрочено (IS_DELAY)
                      val isSpecialControlGoods: Boolean, //Индикатор: Есть товары  особого контроля (IS_SPECIAL)
                      val isRawMaterials: Boolean,     //Индикатор: Сырье (IS_RAWMAT)
                      val isUFF: Boolean,              //Индикатор: UFF (фрукты, овощи) (IS_UFF)
                      val isAlco: Boolean,             //Индикатор:  Алкоголь (IS_ALCO)
                      val isSupplierReturnAvailability: Boolean, //Индикатор: Наличие возврата поставщика (IS_RET)
                      val isNotEDI: Boolean,           //Индикатор: EDI исключение (IS_NOT_EDI)
                      val isPromo: Boolean,            //Индикатор: Промо (IS_ADV)
                      val isRecount: Boolean,          //Индикатор: Пересчетная ГЕ (IS_COUNT)
                      val isOwnTransport: Boolean,     //Индикатор: Собственный транспорт (IS_OWN)
                      val isEDO: Boolean,               //Индикатор ЭДО
                      val quantityOutgoingFillings: Int //Количество исходящих пломб (задания ПРЦ, EV_NUM_SEALS из ZMP_UTZ_GRZ_21_V001 и ZMP_UTZ_GRZ_28_V001)
) {

    companion object {
        fun from(restData: TaskDescriptionRestInfo): TaskDescription {
            return TaskDescription(currentStatus = TaskStatus.from(restData.currentStatusCode),
                    currentStatusText = restData.currentStatusText,
                    currentStatusDate = restData.currentStatusDate,
                    currentStatusTime = restData.currentStatusTime,
                    nextStatusText = restData.nextStatusText,
                    ttnNumber = restData.ttnNumber,
                    orderNumber = restData.orderNumber,
                    deliveryNumber = restData.deliveryNumber ?: "",
                    plannedDeliveryDate = restData.plannedDeliveryDate,
                    plannedDeliveryTime = restData.plannedDeliveryTime,
                    actualArrivalDate = restData.actualArrivalDate,
                    actualArrivalTime = restData.actualArrivalTime,
                    quantityPositions = restData.quantityPositions.toInt(),
                    isOverdue = restData.isOverdue.isNotEmpty(),
                    isSpecialControlGoods = restData.isSpecialControlGoods.isNotEmpty(),
                    isRawMaterials = restData.isRawMaterials.isNotEmpty(),
                    isUFF = restData.isUFF.isNotEmpty(),
                    isAlco = restData.isAlco.isNotEmpty(),
                    isSupplierReturnAvailability = restData.isSupplierReturnAvailability.isNotEmpty(),
                    isNotEDI = restData.isNotEDI.isNotEmpty(),
                    isPromo = restData.isPromo.isNotEmpty(),
                    isRecount = restData.isRecount.isNotEmpty(),
                    isOwnTransport = restData.isOwnTransport.isNotEmpty(),
                    isEDO = restData.isEDO.isNotEmpty(),
                    nextStatusDate = "",
                    nextStatusTime = "",
                    quantityOutgoingFillings = 0
            )
        }
    }
}

data class TaskDescriptionRestInfo(
        @SerializedName("CUR_STAT")
        val currentStatusCode: String,
        @SerializedName("CUR_ST_TEXT")
        val currentStatusText: String,
        @SerializedName("CUR_ST_DATE")
        val currentStatusDate: String,
        @SerializedName("CUR_ST_TIME")
        val currentStatusTime: String,
        @SerializedName("NEXT_ST_TEXT")
        val nextStatusText: String,
        @SerializedName("ZTTN")
        val ttnNumber: String,
        @SerializedName("EBELN")
        val orderNumber: String,
        @SerializedName("VBELN")
        val deliveryNumber: String?,
        @SerializedName("DATE_PLAN")
        val plannedDeliveryDate: String,
        @SerializedName("TIME_PLAN")
        val plannedDeliveryTime: String,
        @SerializedName("DATE_FACT")
        val actualArrivalDate: String,
        @SerializedName("TIME_FACT")
        val actualArrivalTime: String,
        @SerializedName("QNT_POS")
        val quantityPositions: String,
        @SerializedName("IS_DELAY")
        val isOverdue: String,
        @SerializedName("IS_SPECIAL")
        val isSpecialControlGoods: String,
        @SerializedName("IS_RAWMAT")
        val isRawMaterials: String,
        @SerializedName("IS_UFF")
        val isUFF: String,
        @SerializedName("IS_ALCO")
        val isAlco: String,
        @SerializedName("IS_RET")
        val isSupplierReturnAvailability: String,
        @SerializedName("IS_NOT_EDI")
        val isNotEDI: String,
        @SerializedName("IS_ADV")
        val isPromo: String,
        @SerializedName("IS_COUNT")
        val isRecount: String,
        @SerializedName("IS_OWN")
        val isOwnTransport: String,
        @SerializedName("IS_EDO")
        val isEDO: String
) {
}



