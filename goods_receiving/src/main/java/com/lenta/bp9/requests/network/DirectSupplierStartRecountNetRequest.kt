package com.lenta.bp9.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class DirectSupplierStartRecountNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<DirectSupplierStarRecountRestInfo, DirectSupplierStarRecountParams>() {
    override suspend fun run(params: DirectSupplierStarRecountParams): Either<Failure, DirectSupplierStarRecountRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_11_V001", params, DirectSupplierStarRecountStatus::class.java)
    }
}

/**class DirectSupplierStartRecountNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<DirectSupplierStarRecountRestInfo, DirectSupplierStarRecountParams>() {
    override suspend fun run(params: DirectSupplierStarRecountParams): Either<Failure, DirectSupplierStarRecountRestInfo> {

        val webCallParams = WebCallParams().apply {
        data = gson.toJson(params)
        headers = mapOf(
        "X-SUP-DOMAIN" to "DM-MAIN",
        "Content-Type" to "application/json",
        "Web-Authorization" to sessionInfo.basicAuth
        )
        }

        val resString = hyperHive.requestAPI.web("ZMP_UTZ_GRZ_11_V001", webCallParams). execute()
        Logg.d { "resString: $resString" }

        return hyperHive.requestAPI.web("ZMP_UTZ_100_V001", webCallParams).execute().toFmpObjectRawStatusEither(DirectSupplierStarRecountStatus::class.java, gson)
    }
}*/

data class DirectSupplierStarRecountParams(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String, //Номер задания
        @SerializedName("IV_IP")
        val ip: String, //Ip адрес ТСД
        @SerializedName("IV_PERNR")
        val personnelNumber: String, //Табельный номер
        @SerializedName("IV_DATE_COUNT")
        val dateRecount: String, //Дата начала пересчета
        @SerializedName("IV_TIME_COUNT")
        val timeRecount: String, //Время начала пересчета
        @SerializedName("IV_UNBIND_VSD")
        val unbindVSD: String //общий флаг
)

class DirectSupplierStarRecountStatus : ObjectRawStatus<DirectSupplierStarRecountRestInfo>()

data class DirectSupplierStarRecountRestInfo(
        @SerializedName("ET_TASK_POS") //Таблица состава задания ППП	ZTT_GRZ_TASK_DS_POS_EXCH
        val taskComposition: String,
        @SerializedName("ET_TASK_DIFF") //Таблица расхождений по товару	ZTT_GRZ_TASK_DIF_EXCH
        val productDiscrepancies: List<List<String>>,
        @SerializedName("ET_TASK_PARTS") //Таблица партий задания	ZTT_GRZ_TASK_PARTS_EXCH
        val taskParty: String,
        @SerializedName("ET_PARTS_DIFF") //Таблица расхождений по партиям	ZTT_GRZ_PARTS_DIF_EXCH
        val partyDiscrepancies: String,
        @SerializedName("ET_PROD_TEXT") //Таблица названий производителей	ZTT_GRZ_PROD_TEXT
        val manufacturersNames: String,
        @SerializedName("ET_TASK_SETS") //список наборов
        val taskSets: String,
        @SerializedName("ET_TASK_BOX") //список коробок задания
        val taskBoxes: String,
        @SerializedName("ET_TASK_MARK") //список марок задания
        val taskStamps: String,
        @SerializedName("ET_MARK_DIFF") //таблица обработанных марок
        val processedStamps: String,
        @SerializedName("ET_MARK_BAD") //таблица плохих марок
        val badStamps: String,
        @SerializedName("ET_BOX_DIFF") //таблица обработанных коробов
        val processedBoxes: String,
        @SerializedName("ET_BOX_DIFF") //структура карточки задания
        val taskStructure: String,
        @SerializedName("ET_VET_NOT_ACTUAL") //таблица неактуальных ВСД
        val vetNotActual: String,
        @SerializedName("ET_VET_DIFF") //таблица расхождений по вет. товарам
        val vetDiscrepancies: String,
        @SerializedName("ET_VBELN_COM") //таблица примечаний к ВП
        val vetNotes: String,
        @SerializedName("EV_RETCODE")
        val retcode: String,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String
)