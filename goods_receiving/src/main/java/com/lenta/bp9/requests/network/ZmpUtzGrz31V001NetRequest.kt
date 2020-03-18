package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskExciseStampInfoRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ZmpUtzGrz31V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz31V001Result, ZmpUtzGrz31V001Params> {
    override suspend fun run(params: ZmpUtzGrz31V001Params): Either<Failure, ZmpUtzGrz31V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_31_V001", params, ZmpUtzGrz31V001Status::class.java)
    }
}

data class ZmpUtzGrz31V001Params(
        @SerializedName("IV_TASK_NUM") //номер задания
        val taskNumber: String,
        @SerializedName("IV_MATNR") //номер товара
        val materialNumber: String,
        @SerializedName("IV_BOX_NUM") //Номер коробки
        val boxNumber: String,
        @SerializedName("IV_MARK_NUM") //Код акцизной марки
        val stampCode: String
)

class ZmpUtzGrz31V001Status : ObjectRawStatus<ZmpUtzGrz31V001Result>()

data class ZmpUtzGrz31V001Result(
        @SerializedName("EV_STAT")
        val indicatorOnePosition: String,
        @SerializedName("EV_EXIDV_TOP") //Номер ГЕ
        val cargoUnitNumber: String,
        @SerializedName("ET_MARKS") //Список марок задания для передачи в МП
        val taskExciseStamps: List<TaskExciseStampInfoRestData>,
        @SerializedName("ET_PROD_TEXT")//Таблица ЕГАИС производителей
        val manufacturers: List<Manufacturer>,
        @SerializedName("ES_MATNR") //Данные по товару излишку для ПГЕ
        val productSurplusDataPGE: ProductSurplusDataPGERestData,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class ProductSurplusDataPGERestData(
        //Номер товара
        @SerializedName("MATNR")
        val materialNumber: String,
        //Длинное наименование
        @SerializedName("NAME")
        val materialName: String,
        //базисная единица измерения
        @SerializedName("MEINS")
        val uom: String,
        @SerializedName("BSTME")
        val purchaseOrderUnits: String, //ЕИ заказа на поставку
        //кол-во вложения
        @SerializedName("QNTINCL")
        val quantityInvestments: String,
        //Индикатор: Алкоголь
        @SerializedName("IS_ALCO")
        val isAlco: String,
        //Признак – товар акцизный
        @SerializedName("IS_EXC")
        val isExc: String,
        @SerializedName("IS_RUS")
        val isRus: String,
        //Признак – веттовар
        @SerializedName("IS_VET")
        val isVet: String
)