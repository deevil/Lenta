package com.lenta.inventory.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class InvSendReportNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<InvSendReportResponse, InventoryReport>() {

    override suspend fun run(params: InventoryReport): Either<Failure, InvSendReportResponse> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_93_V001", params, InvSendReportStatus::class.java)
    }
}

class InvSendReportStatus : ObjectRawStatus<InvSendReportResponse>()

data class InvSendReportResponse(
        //Таблица МХ, не записанных в ERP с ТСД
        @SerializedName("ET_PLACES_NOT_SAVE")
        val notSavedPlaces: List<PlaceInfo>,
        //Время на обработку задания (строка)
        @SerializedName("EV_TIME_OF_PROC")
        val timeOfProcess: String,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String,
        @SerializedName("EV_RETCODE")
        override val retCode: Int

) : SapResponse

//ET_PLACES_NOT_SAVE
data class PlaceInfo(
        @SerializedName("PLACE_CODE")
        val placeCode: String
)


data class InventoryReport(
        //номер ТК
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        // IP адрес ТСД
        @SerializedName("IV_IP")
        val ipAdress: String,
        // Номер задачи
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        // Окончательно
        @SerializedName("IV_FINISH")
        val isFinish: String,
        // Табельный номер
        @SerializedName("IV_PERNR")
        val personnelNumber: String,
        // МХ для обмена с ТСД
        @SerializedName("IT_STORPLACES_DEL")
        val storePlacesForDelete: List<StorePlace>,
        // Список товаров
        @SerializedName("IT_TASK_MATNR_LIST")
        val products: List<MaterialNumber>,
        // Список марок
        @SerializedName("IT_TASK_MARK_LIST")
        val stamps: List<ExciseStampInfo>,
        // Признак пересчета по табельным номерам
        @SerializedName("IV_COUNT_PERNR")
        val isRecount: String
)

//IT_STORPLACES_DEL
data class StorePlace(
        // Склад
        @SerializedName("LGORT")
        val storage: String,
        // Место хранения
        @SerializedName("PLACE_CODE")
        val placeCode: String,
        // Номер товара
        @SerializedName("MATNR")
        val matNumber: String,
        // Индикатор удаления
        @SerializedName("IS_DEL")
        val isDel: String
)


//IT_TASK_MATNR_LIST
data class MaterialNumber(
        @SerializedName("MATNR")
        val materialNumber: String, //Номер товара
        @SerializedName("PLACE_CODE")
        val storePlaceCode: String, //Код места хранения
        @SerializedName("FACT_QNT")
        val factQuantity: String, //УТЗ ТСД: Фактическое количество в БЕИ (первый подсчет)
        @SerializedName("XZAEL")
        val positionCounted: String, //УТЗ ТСД: Инв.: Позиция подсчитана (первый подсчет)
        @SerializedName("IS_DEL")
        val isDel: String, //Общий флаг
        @SerializedName("IS_SET")
        val isSet: String, //Общий флаг
        @SerializedName("IS_EXC_OLD")
        val isExcOld: String //Общий флаг
)

//IT_TASK_MARK_LIST
data class ExciseStampInfo(
        @SerializedName("MATNR")
        val productNumber: String, //Номер товара
        @SerializedName("PLACE_CODE")
        val storePlaceCode: String, //Код места хранения
        @SerializedName("MARK_NUM")
        val stampNumber: String, //Код акцизной марки
        @SerializedName("BOX_NUM")
        val boxNumber: String, //Номер коробки
        @SerializedName("MATNR_OSN")
        val productNumberOSN: String, //Номер товара ??
        @SerializedName("ZPROD")
        val organizationCodeEGAIS: String, //ЕГАИС Код организации
        @SerializedName("BOTT_MARK")
        val dateOfPour: String, //УТЗ ТСД: Дата розлива
        @SerializedName("IS_UNKNOWN")
        val isUnknown: String //Общий флаг
)

