package com.lenta.inventory.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.toFmpObjectRawStatusEither
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class ObtainingDataExciseGoodsNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<ExciseGoodsRestInfo, ExciseGoodsParams>(){
    override suspend fun run(params: ExciseGoodsParams): Either<Failure, ExciseGoodsRestInfo> {

        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        val resString = hyperHive.requestAPI.web("ZMP_UTZ_100_V001", webCallParams). execute()
        Logg.d { "resString: $resString" }

        val res = hyperHive.requestAPI.web("ZMP_UTZ_100_V001", webCallParams).execute().toFmpObjectRawStatusEither(ExciseGoodsStatus::class.java, gson)

        return res
    }
}

data class ExciseGoodsParams(
        @SerializedName("IV_WERKS")
        val werks: String, //Предп
        @SerializedName("IV_MATNR")
        val materialNumber: String, //Номер товара
        @SerializedName("IV_MATNR_COMP")
        val materialNumberComp: String, //Номер компонета набора
        @SerializedName("IV_MARK_NUM")
        val markNumber: String, //Код акцизной марки
        @SerializedName("IV_BOX_NUM")
        val boxNumber: String, //Номер коробки
        @SerializedName("IV_ZPROD")
        val codeEGAIS: String, //ЕГАИС Код организации
        @SerializedName("IV_BOTT_MARK")
        val bottMark: String, //УТЗ ТСД: Дата розлива
        @SerializedName("IV_MODE")
        val mode: String, //Индикатор из одной позиции
        @SerializedName("IV_CODEBP")
        val codeEBP: String, //Код БП УТЗ
        @SerializedName("IV_FACT_QNT")
        val factCount: String) //Фактическое количество (для партионных)

class ExciseGoodsStatus : ObjectRawStatus<ExciseGoodsRestInfo>()

data class ExciseGoodsRestInfo(
        @SerializedName("EV_STAT")
        val status: String, //Поле CHAR3
        @SerializedName("EV_STAT_TEXT")
        val statusTxt: String, //Текст статуса для отображения в МП
        @SerializedName("ET_MARKS")
        val stamps: List<StampsBox>, //Таблица марок в коробке
        @SerializedName("EV_MATNR_COMP")
        val materialNumber: String, //Номер товара
        @SerializedName("ET_PROD_TEXT")
        val manufacturers: List<Manufacturer>, //Таблица ЕГАИС производителей
        @SerializedName("EV_ZCHARG")
        val batchNumber: String, //Номер партии
        @SerializedName("EV_DATEOFPOUR")
        val dateManufacture: String, //Дата производства
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String, //Текст ошибки
        @SerializedName("EV_RETCODE")
        val retCode: String) //Код возврата для ABAP-операторов

data class StampsBox(
        @SerializedName("BOX_NUM")
        val boxNumber: String, //Номер коробки
        @SerializedName("MARK_NUM")
        val exciseStampCode: String) //Код акцизной марки
