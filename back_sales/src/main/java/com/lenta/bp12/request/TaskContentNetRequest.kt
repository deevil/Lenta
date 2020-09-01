package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.request.pojo.*
import com.lenta.bp12.request.pojo.taskContentNetRequest.MrcInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

/**
 * ZMP_UTZ_BKS_03_V001
 * "Получение состава задания"
 **/
class TaskContentNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<TaskContentResult, TaskContentParams> {

    override suspend fun run(params: TaskContentParams): Either<Failure, TaskContentResult> {
        return fmpRequestsHelper.restRequest(RESOURCE_NAME, params, TaskContentStatus::class.java)
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_BKS_03_V001"
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
        val isNeedAdditionalData: String = false.toSapBooleanString()
)

class TaskContentStatus : ObjectRawStatus<TaskContentResult>()

data class TaskContentResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val positions: List<PositionInfo>?,
        /** Данные состава корзин задания */
        @SerializedName("ET_TASK_BASKET_POS")
        val basketProducts: List<BasketPositionInfo>?,
        /** Данные корзин задания для обмена с МП */
        @SerializedName("ET_TASK_BASKET")
        val basketInfo: List<CreateTaskBasketInfo>?,
        /** Таблица марок задания */
        @SerializedName("ET_TASK_MARK")
        val exciseMarks: List<ExciseMarkInfo>?,
        /** Таблица партий */
        @SerializedName("ET_TASK_PARTS")
        val parts: List<PartInfo>?,
        /** Справочные данные товара */
        @SerializedName("ET_MATERIALS")
        val additionalInfo: List<AdditionalInfo>?,
        /** Таблица производителей */
        @SerializedName("ET_PROD")
        val producers: List<TaskProducerInfo>?,
        @SerializedName("ET_MPR")
        val mrcList: List<MrcInfo>?,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse