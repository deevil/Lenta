package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.exception.InfoFailure
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** "ZMP_UTZ_100_V001" «Получение данных по акцизному товару»*/

class ObtainingDataExciseGoodsNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<ExciseGoodsRestInfo, ExciseGoodsParams> {

    override suspend fun run(params: ExciseGoodsParams): Either<Failure, ExciseGoodsRestInfo> {
        val result = fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = ExciseGoodsStatus::class.java
        )
        return if (result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
            Either.Left(InfoFailure(result.b.errorTxt.orEmpty()))
        } else result

    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_100_V001"
        private const val NON_FAILURE_RET_CODE = "0"
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
        val stampCode: String, //Код акцизной марки
        @SerializedName("IV_BOX_NUM")
        val boxNumber: String, //Номер коробки
        @SerializedName("IV_ZPROD")
        val manufacturerCode: String, //ЕГАИС Код организации
        @SerializedName("IV_BOTT_MARK")
        val bottlingDate: String, //УТЗ ТСД: Дата розлива
        @SerializedName("IV_MODE")
        val mode: String, //Индикатор из одной позиции
        @SerializedName("IV_CODEBP")
        val codeEBP: String, //Код БП УТЗ
        @SerializedName("IV_FACT_QNT")
        val factCount: String
) //Фактическое количество (для партионных)

class ExciseGoodsStatus : ObjectRawStatus<ExciseGoodsRestInfo>()

data class ExciseGoodsRestInfo(
        @SerializedName("EV_STAT")
        val status: InfoStatus?, //Поле CHAR3
        @SerializedName("EV_STAT_TEXT")
        val statusTxt: String?, //Текст статуса для отображения в МП
        @SerializedName("ET_MARKS")
        val stampsBox: List<StampsBox>?, //Таблица марок в коробке
        @SerializedName("EV_MATNR_COMP")
        val materialNumber: String?, //Номер товара
        @SerializedName("ET_PROD_TEXT")
        val manufacturers: List<Manufacturer>?, //Таблица ЕГАИС производителей
        @SerializedName("EV_ZCHARG")
        val batchNumber: String?, //Номер партии
        @SerializedName("EV_DATEOFPOUR")
        val dateManufacture: String?, //Дата производства
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String?, //Текст ошибки
        @SerializedName("EV_RETCODE")
        val retCode: String? //Код возврата для ABAP-операторов
) {
    enum class InfoStatus {
        @SerializedName("01")
        StampFound,

        @SerializedName("02")
        StampOverload,

        @SerializedName("03")
        StampOfOtherProduct,

        @SerializedName("04")
        StampNotFound
    }
}

data class StampsBox(
        @SerializedName("BOX_NUM")
        val boxNumber: String, //Номер коробки
        @SerializedName("MARK_NUM")
        val exciseStampCode: String
) //Код акцизной марки
