package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.exception.InfoFailure
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_08_V001	«Начало консолидации» */
class StartConsolidation @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<StartConsolidationResult, StartConsolidationParams> {

    override suspend fun run(params: StartConsolidationParams): Either<Failure, StartConsolidationResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = StartConsolidationStatus::class.java
        ).let { result ->
            if(result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
                Either.Left(InfoFailure(result.b.errorTxt))
            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_08_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

data class StartConsolidationParams(
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

class StartConsolidationStatus : ObjectRawStatus<StartConsolidationResult>()

data class StartConsolidationResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val taskComposition: List<TaskComposition>,

        /** Список ЕО */
        @SerializedName("ET_TASK_EXIDV")
        val eoList: List<ProcessingUnit>,

        /** Список ГЕ */
        @SerializedName("ET_TASK_EXIDV_TOP")
        val geList: List<CargoUnit>,

        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String
) {
    /**"ET_TASK_POS"*/
    data class TaskComposition(
            /** SAP-код товара */
            @SerializedName("MATNR")
            val materialNumber: String,

            /** Номер ЕО */
            @SerializedName("EXIDV")
            val processingUnitNumber: String,

            /** Единица измерения заказа на поставку */
            @SerializedName("BSTME")
            val orderUnits: String,

            /** Объем заказа */
            @SerializedName("MENGE")
            val quantity: String)

    /** ET_TASK_EXIDV Список ЕО*/
    data class ProcessingUnit(
            /** Номер ЕО */
            @SerializedName("EXIDV")
            val processingUnitNumber: String,

            /** Номер корзины */
            @SerializedName("BASKET_NUM")
            val basketNumber: String,

            /** Поставщик */
            @SerializedName("LIFNR")
            val supplier: String,

            /** Флаг – «Алкоголь» */
            @SerializedName("IS_ALCO")
            val isAlco: String,

            /** Флаг – «Обычный товар» */
            @SerializedName("IS_USUAL")
            val isUsual: String,

            /** Количество позиций */
            @SerializedName("QNT_SKU")
            val quantity: String)

    /**"ET_TASK_EXIDV_TOP" Список ГЕ*/
    data class CargoUnit(
            /** Номер ГЕ */
            @SerializedName("EXIDV_TOP")
            val cargoUnitNumber: String,

            /** Номер ЕО */
            @SerializedName("EXIDV")
            val processingUnitNumber: String
    )

}

