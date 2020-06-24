package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.models.core.Supplier
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_03_V001	«Получение состава задания» */
class ObtainingTaskComposition @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<TaskCompositionResult, TaskCompositionParams> {

    override suspend fun run(params: TaskCompositionParams): Either<Failure, TaskCompositionResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = "ZMP_UTZ_MVM_03_V001",
                data = params,
                clazz = TaskStatus::class.java
        ).let {
            return@let it
        }
    }

}

data class TaskCompositionParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,

        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,

        /** Режим работы:
         * 1 - получение состава задания
         * 2 - получение состава задания с переблокировкой
         */
        @SerializedName("IV_MODE")
        val mode: Int,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String,

        /**Необходимость заполнения справочных данных*/
        @SerializedName("IV_MATNR_DATA_FLG")
        val withProductInfo: String
)

class TaskStatus : ObjectRawStatus<TaskCompositionResult>()

data class TaskCompositionResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val taskComposition: List<TaskComposition>,

        /** Таблица корзин задания*/
        @SerializedName("ET_TASK_BASKET")
        val basketList: List<TaskBasket>,

        @SerializedName("ET_TASK_MARK")
        val stampsList: List<TaskExciseStampInfoRestData>,

        @SerializedName("ET_TASK_PARTS")
        val taskBatches: List<TaskBatchInfoRestData>,

        @SerializedName("ET_MATERIALS")
        val fullProductsInfoList: List<TaskFullProductInfo>,

        @SerializedName("ET_PROD")
        val manufacturers: List<Manufacturer>,

        @SerializedName("ET_LIFNR")
        val suppliers: List<Supplier>
) {

    /**"ET_TASK_POS"*/
    data class TaskComposition(
            /** SAP-код товара */
            @SerializedName("MATNR")
            val materialNumber: String,

            /** Индикатор: Позиция  посчитана*/
            @SerializedName("XZAEL")
            val positionCounted: String,

            /** Количество вложенного */
            @SerializedName("QNTINCL")
            val quantityInvestments: String,

            /** Единица измерения заказа на поставку */
            @SerializedName("BSTME")
            val orderUnits: String,

            /** Индикатор: Товар «Еда» */
            @SerializedName("IS_FOOD")
            val isFood: String,

            /** Объем заказа */
            @SerializedName("MENGE")
            val quantity: String,

            /** Рекомендуемая дата с */
            @SerializedName("REQ_DATE_FROM")
            val recommendedDateFrom: String,

            /** Рекомендуемая дата по */
            @SerializedName("REQ_DATE_TO")
            val recommendedDateTO: String,

            /** Объем БЕИ */
            @SerializedName("VOLUM")
            val volume: String
    )

    /**"ET_TASK_BASKET"*/
    data class TaskBasket(
            /** SAP-код товара */
            @SerializedName("MATNR")
            val materialNumber: String,

            /** Номер корзины */
            @SerializedName("BASKET_NUM")
            val basketNumber: String,

            /** Посчитанное количество */
            @SerializedName("FACT_QNT")
            val quantity: String,

            /** Индикатор: позиция посчитана */
            @SerializedName("XZAEL")
            val positionCounted: String,

            /** Базисная единица измерения */
            @SerializedName("MEINS")
            val uom: String,

            /** Поставщик */
            @SerializedName("LIFNR")
            val supplier: String,

            /** Флаг – «Марочные» остатки */
            @SerializedName("IS_MARK_STOCKS")
            val isExcise: String,

            /** Флаг – «Партионные» остатки */
            @SerializedName("IS_PARTS_STOCKS")
            val isNotExcise: String,

            /** Флаг – «Алкоголь» */
            @SerializedName("IS_ALCO")
            val isAlco: String,

            /** Флаг – «Обычный товар» */
            @SerializedName("IS_USUAL")
            val isUsual: String,

            /** Флаг – «Меркурианский товар» */
            @SerializedName("IS_VET")
            val isVet: String,

            /** Номер партии */
            @SerializedName("ZCHARG")
            val batchNumber: String,

            /** Вид товара */
            @SerializedName("MTART")
            val materialType: String,

            /** Флаг – «Еда» */
            @SerializedName("IS_FOOD")
            val isFood: String
    )

    /**"ET_TASK_MARK"*/
    data class TaskExciseStampInfoRestData(
            /** SAP-код товара */
            @SerializedName("MATNR") //Номер товара
            val materialNumber: String,

            /** Поставщик */
            @SerializedName("LIFNR")
            val supplier: String,

            /** Код акцизной марки */
            @SerializedName("MARK_NUM")
            val code: String,

            /** Номер коробки */
            @SerializedName("BOX_NUM")
            val boxNumber: String,

            /** Проблемная марка */
            @SerializedName("IS_MARK_BAD")
            val isMarkBad: String,

            /** Номер корзины */
            @SerializedName("BASKET_NUM")
            val basketNumber: String
    )

    /**"ET_TASK_PARTS"*/
    data class TaskBatchInfoRestData(
            /** SAP-код товара */
            @SerializedName("MATNR")
            val materialNumber: String,

            /** Производитель товара */
            @SerializedName("ZPROD")
            var organizationCodeEGAIS: String,

            /** Дата производства */
            @SerializedName("DATEOFPOUR")
            val bottlingDate: String,

            /** Номер корзины */
            @SerializedName("FACT_QNT")
            val factQuantity: String,

            /** Номер корзины */
            @SerializedName("ZCHARG")
            val batchNumber: String,

            /** Номер корзины */
            @SerializedName("BASKET_NUM")
            val basketNumber: String
    )

    /**"ET_MATERIALS"*/
    data class TaskFullProductInfo(
            /** SAP-код товара */
            @SerializedName("MATNR")
            val materialNumber: String,

            /** Длинное наименование */
            @SerializedName("NAME")
            val name: String,

            /** Вид товара */
            @SerializedName("MATYPE")
            val productType: String,

            /** Базисная единица измерения */
            @SerializedName("BUOM")
            val units: String, 

            /** Тип матрицы SKU */
            @SerializedName("MATR_TYPE")
            val matrixTypeSKU: String,

            /** Номер отдела (секция) */
            @SerializedName("ABTNR")
            val departmentNumber: String,

            /** Признак – товар акцизный */
            @SerializedName("IS_EXC")
            val isExc: String,

            /** Признак - товар алкогольный */
            @SerializedName("IS_ALCO")
            val isAlco: String,

            /** Группа товаров */
            @SerializedName("MATKL")
            val goodGroup: String,

            /** Группа закупок */
            @SerializedName("EKGRP")
            val purchaseGroup: String,

            /** Единица измерения заказа */
            @SerializedName("BSTME")
            val orderUnits: String,

            /** Количество вложений */
            @SerializedName("QNTINCL")
            val quantityInvestments: String,

            /** Признак: отечественный */
            @SerializedName("IS_RUS")
            val isRus: String,

            /** Признак: ветеринарный контроль*/
            @SerializedName("IS_VET")
            val isVet: String,

            /** Индикатор: Товар «Еда» */
            @SerializedName("IS_FOOD")
            val isFood: String,

            /** Объем в куб.метрах  (точность 6 знаков) */
            @SerializedName("VOLUM")
            val volume: String,

            /** Единица объема */
            @SerializedName("VOLEH")
            val volumeUnit: String,

            /** Тип маркировки */
            @SerializedName("ZMARKTYPE")
            val markType: String)
}

