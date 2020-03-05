package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.model.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class TaskContentNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<TaskContentResult, TaskContentParams> {

    override suspend fun run(params: TaskContentParams): Either<Failure, TaskContentResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_BKS_03_V001", params, TaskContentStatus::class.java)
    }

}

data class TaskContentParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** Режим работы: 1 - получение состава задания, 2 - получение состава задания с переблокировкой */
        @SerializedName("IV_MODE")
        val mode: Int,
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** Необходимость заполнения справочных данных */
        @SerializedName("IV_MATNR_DATA_FLG")
        val isNeedAdditionalData: Boolean
)

class TaskContentStatus : ObjectRawStatus<TaskContentResult>()

data class TaskContentResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val goods: List<GoodItem>,
        /** Таблица марок задания */
        @SerializedName("ET_TASK_MARK")
        val marks: List<MarkItem>,
        /** Таблица партий */
        @SerializedName("ET_TASK_PARTS")
        val shipments: List<ShipmentItem>,
        /** Справочные данные товара */
        @SerializedName("ET_MATERIALS")
        val additionalInfo: List<AdditionalInfoItem>,
        /** Таблица производителей */
        @SerializedName("ET_PROD")
        val producers: List<ProdItem>,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse