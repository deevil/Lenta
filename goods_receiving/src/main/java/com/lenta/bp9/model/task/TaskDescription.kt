package com.lenta.bp9.model.task

//ТП. Таблица 75 ZSGRZ_TASK_EXCH Структура карточки задания
data class TaskDescription(val currentStatusCode: String,   //Код текущего статуса (CUR_STAT)
                      val currentStatusText: String,   //Текс текущего статуса (CUR_ST_TEXT)
                      val currentStatusDate: String,   //Дата текущего статуса (CUR_ST_DATE)
                      val currentStatusTime: String,   //Время текущего статуса (CUR_ST_TIME)
                      val nextStatusText: String,      //Следующий статус (NEXT_ST_TEXT)
                      val ttnNumber: String,           //Номер ТН\ТТН (ZTTN)
                      val orderNumber: String,         //Номер заказа (EBELN)
                      val deliveryNumber: String,      //Номер поставки\Транспортировки (VBELN)
                      val plannedDeliveryDate: String, //Плановая дата поставки (DATE_PLAN)
                      val plannedDeliveryTime: String, //Плановое время поставки (TIME_PLAN)
                      val actualArrivalDate: String,   //Фактическая дата прибытия (DATE_FACT)
                      val actualArrivalTime: String,   //Фактическое время прибытие (TIME_FACT)
                      val quantityPositions: String,   //Количество позиций (QNT_POS)
                      val isOverdue: Boolean,          //Индикатор: Просрочено (IS_DELAY)
                      val isSpecialControlGoods: Boolean, //Индикатор: Есть товары  особого контроля (IS_SPECIAL)
                      val isRawMaterials: Boolean,     //Индикатор: Сырье (IS_RAWMAT)
                      val isUFF: Boolean,              //Индикатор: UFF (фрукты, овощи) (IS_UFF)
                      val isAlco: Boolean,             //Индикатор:  Алкоголь (IS_ALCO)
                      val isSupplierReturnAvailability: Boolean, //Индикатор: Наличие возврата поставщика (IS_RET)
                      val isNotEDI: Boolean,           //Индикатор: EDI исключение (IS_NOT_EDI)
                      val isPromo: Boolean,            //Индикатор: Промо (IS_ADV)
                      val isRecount: Boolean,          //Индикатор: Пересчетная ГЕ (IS_COUNT)
                      val isOwnTransport: Boolean     //Индикатор: Собственный транспорт (IS_OWN)
                     ) {
}


