package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.revise.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class TaskContentsNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<TaskContentsRequestResult, TaskContentRequestParameters> {
    override suspend fun run(params: TaskContentRequestParameters): Either<Failure, TaskContentsRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_15_V001", params, TaskContentsRequestStatus::class.java)
    }
}

class TaskContentsRequestStatus : ObjectRawStatus<TaskContentsRequestResult>()


data class TaskContentsRequestResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_TASK_POS") //Таблица состава задания ППП	ZTT_GRZ_TASK_DS_POS_EXCH
        val taskComposition: List<TaskComposition>,
        @SerializedName("ET_TASK_DIFF") //Таблица расхождений по товару	ZTT_GRZ_TASK_DIF_EXCH
        val taskProductDiscrepancies: List<TaskProductDiscrepanciesRestData>,
        @SerializedName("ET_TASK_PARTS") //Таблица партий задания
        val taskBatches: List<TaskBatchInfoRestData>,
        @SerializedName("ET_PARTS_DIFF") //Таблица расхождений по партиям
        val taskBatchesDiscrepancies: List<TaskBatchesDiscrepanciesRestData>,
        @SerializedName("ET_VET_DIFF") //Таблица расхождений по вет.товарам
        val taskMercuryInfoRestData: List<TaskMercuryInfoRestData>,
        @SerializedName("ET_VET_NOT_ACTUAL") //Список не актуальных ВСД
        val taskMercuryNotActualRestData: List<TaskMercuryNotActualRestData>,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("ET_DOC_NOTIFY")
        val documentNotifications: List<TaskNotificationRestInfo>,
        @SerializedName("ET_MATNR_NOTIFY")
        val productNotifications: List<TaskNotificationRestInfo>,
        @SerializedName("ET_COND_NOTIFY")
        val conditionNotifications: List<TaskNotificationRestInfo>,
        @SerializedName("ET_DOC_CHK")
        val deliveryDocumentsRevise: List<DeliveryDocumentReviseRestData>,
        @SerializedName("ET_DOC_MATNR_CHK")
        val deliveryProductDocumentsRevise: List<DeliveryProductDocumentReviseRestData>,
        @SerializedName("ET_PARTS_CHK")
        val productBatchesRevise: List<ProductBatchReviseRestData>,
        @SerializedName("ET_FORMA_RUS")
        val formsABRussianRevise: List<FormABRussianReviseRestData>,
        @SerializedName("ET_FORMA_IMP")
        val formsABImportRevise: List<FormABImportReviseRestData>,
        @SerializedName("ET_COMP_CHK")
        val setComponentsRevise: List<SetComponentReviseRestData>,
        @SerializedName("ES_TTN_CHK")
        val invoiceRevise: InvoiceReviseRestData,
        @SerializedName("ET_VBELN_COM")
        val commentsToVP: List<CommentToVPRestData>,
        @SerializedName("ET_VET_CHK")
        val productsVetDocumentRevise: List<ProductVetDocumentReviseRestData>,
        @SerializedName("ET_CMPLX_DOC")
        val complexDocumentsRevise: List<ComplexDocumentReviseRestData>,
        @SerializedName("ET_COND_CHECK")
        val transportConditions: List<TransportConditionRestData>,
        @SerializedName("ET_TASK_PERNR")
        val sectionsInfo: List<TaskSectionRestData>,
        @SerializedName("ET_MATNR_ABTNR")
        val sectionProducts: List<TaskSectionProductsRestData>,
        @SerializedName("ET_PROD_TEXT")//Таблица ЕГАИС производителей
        val manufacturers: List<Manufacturer>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class TaskContentRequestParameters(
        @SerializedName("IV_MODE")
        val mode: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String
)