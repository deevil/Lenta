package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

//Таблица ES_TASK (ET_TASK) Структура карточки задания
data class TaskDescription(
        val currentStatus: TaskStatus,   //Код текущего статуса (CUR_STAT)
        val currentStatusText: String,   //Текс текущего статуса (CUR_ST_TEXT)
        var currentStatusDate: String,   //Дата текущего статуса (CUR_ST_DATE)
        var currentStatusTime: String,   //Время текущего статуса (CUR_ST_TIME)
        val nextStatusText: String,      //Следующий статус (NEXT_ST_TEXT)
        var nextStatusDate: String,      //Дата следуюшего статуса, изначальна равна текущей дате, может быть изменена пользователем
        var nextStatusTime: String,      //Время следуюшего статуса, изначальна равно текущму времени, может быть изменено пользователем
        val tnNumber: String,           //Номер ТН (TN_NUM)
        val ttnNumber: String,           //Номер ТТН (ZTTN)
        val orderNumber: String,         //Номер заказа (EBELN)
        val deliveryNumber: String,      //Номер поставки\Транспортировки (VBELN)
        val shipmentOrder: String,           //привозит данные для поля "Заказ" (ОРЦ, https://trello.com/c/7pJ5ckNF)
        val shipmentDelivery: String,        //привозит данные для поля "Исходящая поставка" (ОРЦ, https://trello.com/c/7pJ5ckNF)
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
        val cargoUnitNumber: String,     //Номер ГЕ
        val isRecount: Boolean,          //Индикатор: Пересчетная ГЕ (IS_COUNT)
        val isOwnTransport: Boolean,     //Индикатор: Собственный транспорт (IS_OWN)
        val isEDO: Boolean,               //Индикатор ЭДО
        val isVet: Boolean,              //Индикатор Меркурий
        val isBksDiff: Boolean,          //наличие расхождения количества по товарам
        val isSkipCountMan: Boolean,     //Доступен ручной пропуск пересчета
        val countGE: String, //Количество ГЕ в задании ОРЦ
        val countEO: String, //Количество ЕО в задании ОРЦ
        val transportationNumber: String, //Номер транспортировки в задании ОРЦ
        val deliveryNumberOTM: String, //Номер поставки OTM в задании ОРЦ
        val submergedGE: String, //Погруженные ГЕ в задании ОРЦ
        val quantityOutgoingFillings: Int, //Количество исходящих пломб (задания ПРЦ, EV_NUM_SEALS из ZMP_UTZ_GRZ_21_V001 и ZMP_UTZ_GRZ_28_V001)
        val quantityST: Double, //Количество в задании, ШТ
        val quantityKG: Double, //Количество в задании, КГ
        val quantityAll: Double, //Общее количество в задании
        val isBksTN: Boolean,
        val isWO: Boolean, //Товары для автосписания
        val isMark: Boolean, //маркированный товар
        val isZBatches: Boolean, //Z-партии
        val supplierName: String //Z-партии для печати этикеток
) {

        companion object {
                fun from(restData: TaskDescriptionRestInfo): TaskDescription {
                        return TaskDescription(currentStatus = TaskStatus.from(restData.currentStatusCode.orEmpty()),
                                currentStatusText = restData.currentStatusText.orEmpty(),
                                currentStatusDate = restData.currentStatusDate.orEmpty(),
                                currentStatusTime = restData.currentStatusTime.orEmpty(),
                                nextStatusText = restData.nextStatusText.orEmpty(),
                                tnNumber = restData.tnNumber.orEmpty(),
                                ttnNumber = restData.ttnNumber.orEmpty(),
                                orderNumber = restData.orderNumber.orEmpty(),
                                deliveryNumber = restData.deliveryNumber.orEmpty(),
                                shipmentOrder = restData.shipmentOrder.orEmpty(),
                                shipmentDelivery = restData.shipmentDelivery.orEmpty(),
                                plannedDeliveryDate = restData.plannedDeliveryDate.orEmpty(),
                                plannedDeliveryTime = restData.plannedDeliveryTime.orEmpty(),
                                actualArrivalDate = restData.actualArrivalDate.orEmpty(),
                                actualArrivalTime = restData.actualArrivalTime.orEmpty(),
                                quantityPositions = restData.quantityPositions?.toInt() ?: 0,
                                isOverdue = restData.isOverdue?.isNotEmpty() == true,
                                isSpecialControlGoods = restData.isSpecialControlGoods?.isNotEmpty() == true,
                                isRawMaterials = restData.isRawMaterials?.isNotEmpty() == true,
                                isUFF = restData.isUFF?.isNotEmpty() == true,
                                isAlco = restData.isAlco?.isNotEmpty() == true,
                                isSupplierReturnAvailability = restData.isSupplierReturnAvailability?.isNotEmpty() == true,
                                isNotEDI = restData.isNotEDI?.isNotEmpty() == true,
                                isPromo = restData.isPromo?.isNotEmpty() == true,
                                cargoUnitNumber = restData.cargoUnitNumber.orEmpty(),
                                isRecount = restData.isRecount?.isNotEmpty() == true,
                                isOwnTransport = restData.isOwnTransport?.isNotEmpty() == true,
                                isEDO = restData.isEDO?.isNotEmpty() == true,
                                isVet = restData.isVet?.isNotEmpty() == true,
                                isBksDiff = restData.isBksDiff?.isNotEmpty() == true,
                                isSkipCountMan = restData.isSkipCountMan?.isNotEmpty() == true,
                                countGE = restData.countGE.orEmpty(),
                                countEO = restData.countEO.orEmpty(),
                                transportationNumber = restData.transportationNumber.orEmpty(),
                                deliveryNumberOTM = restData.deliveryNumberOTM.orEmpty(),
                                submergedGE = restData.submergedGE.orEmpty(),
                                nextStatusDate = "",
                                nextStatusTime = "",
                                quantityOutgoingFillings = 0,
                                quantityST = restData.quantityST?.toDouble() ?: 0.0,
                                quantityKG = restData.quantityKG?.toDouble() ?: 0.0,
                                quantityAll = restData.quantityAll?.toDouble() ?: 0.0,
                                isBksTN = restData.isBksTN?.isNotEmpty() == true,
                                isWO = restData.isWO?.isNotEmpty() == true,
                                isMark = restData.isMark?.isNotEmpty() == true,
                                isZBatches = restData.isZBatches == "X",
                                supplierName = restData.supplierName.orEmpty()
                        )
        }
    }
}

data class TaskDescriptionRestInfo(
        @SerializedName("CUR_STAT")
        val currentStatusCode: String?,
        @SerializedName("CUR_ST_TEXT")
        val currentStatusText: String?,
        @SerializedName("CUR_ST_DATE")
        val currentStatusDate: String?,
        @SerializedName("CUR_ST_TIME")
        val currentStatusTime: String?,
        @SerializedName("NEXT_ST_TEXT")
        val nextStatusText: String?,
        @SerializedName("TN_NUM")
        val tnNumber: String?,
        @SerializedName("ZTTN")
        val ttnNumber: String?,
        @SerializedName("EBELN")
        val orderNumber: String?,
        @SerializedName("VBELN")
        val deliveryNumber: String?,
        @SerializedName("EBELN_STR")
        val shipmentOrder: String?,
        @SerializedName("VBELN_STR")
        val shipmentDelivery: String?,
        @SerializedName("DATE_PLAN")
        val plannedDeliveryDate: String?,
        @SerializedName("TIME_PLAN")
        val plannedDeliveryTime: String?,
        @SerializedName("DATE_FACT")
        val actualArrivalDate: String?,
        @SerializedName("TIME_FACT")
        val actualArrivalTime: String?,
        @SerializedName("QNT_POS")
        val quantityPositions: String?,
        @SerializedName("IS_DELAY")
        val isOverdue: String?,
        @SerializedName("IS_SPECIAL")
        val isSpecialControlGoods: String?,
        @SerializedName("IS_RAWMAT")
        val isRawMaterials: String?,
        @SerializedName("IS_UFF")
        val isUFF: String?,
        @SerializedName("IS_ALCO")
        val isAlco: String?,
        @SerializedName("IS_RET")
        val isSupplierReturnAvailability: String?,
        @SerializedName("IS_NOT_EDI")
        val isNotEDI: String?,
        @SerializedName("IS_ADV")
        val isPromo: String?,
        @SerializedName("EXIDV_TOP")
        val cargoUnitNumber: String?,
        @SerializedName("IS_COUNT")
        val isRecount: String?,
        @SerializedName("IS_OWN")
        val isOwnTransport: String?,
        @SerializedName("IS_EDO")
        val isEDO: String?,
        @SerializedName("IS_VET")
        val isVet: String?,
        @SerializedName("QNT_EXIDV_TOP")
        val countGE: String?,
        @SerializedName("QNT_EXIDV")
        val countEO: String?,
        @SerializedName("TRNUM")
        val transportationNumber: String?,
        @SerializedName("DELIV_OTM")
        val deliveryNumberOTM: String?,
        @SerializedName("LOAD_EXIDV_TOP")
        val submergedGE: String?,
        @SerializedName("IS_BKS_DIFF")
        val isBksDiff: String?,
        @SerializedName("IS_SKIP_COUNT_MAN")
        val isSkipCountMan: String?,
        @SerializedName("QNT_ST")
        val quantityST: String?,
        @SerializedName("QNT_KG")
        val quantityKG: String?,
        @SerializedName("QNT_ALL")
        val quantityAll: String?,
        @SerializedName("IS_BKS_TN")
        val isBksTN: String?,
        @SerializedName("IS_WO")
        val isWO: String?,
        @SerializedName("IS_MARK")
        val isMark: String?,
        @SerializedName("IS_ZPARTS")
        val isZBatches: String?, // Z-партии
        @SerializedName("LIFNR_NAME")
        val supplierName: String? //Z-партии для печати этикеток
)