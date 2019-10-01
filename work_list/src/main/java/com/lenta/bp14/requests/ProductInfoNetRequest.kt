package com.lenta.bp14.requests

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class ProductInfoNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ProductInfoResult, ProductInfoParams> {
    override suspend fun run(params: ProductInfoParams): Either<Failure, ProductInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_11_V001", params, ProductInfoStatus::class.java)
                .rightToLeft(
                        fnRtoL = {
                                if (it.productsInfo.isNullOrEmpty()) {
                                        return@rightToLeft Failure.GoodNotFound
                                }
                                else null
                        }
                )
    }

}

data class ProductInfoParams(
        @SerializedName("IV_MATNR_DATA_FLG")
        val withProductInfo: String,
        @SerializedName("IV_RBSINFO_FLG")
        val withAdditionalInf: String,
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        @SerializedName("IV_TASK_TYPE")
        val taskType: String,
        @SerializedName("IT_EAN_LIST")
        val eanList: List<EanParam>? = null,
        @SerializedName("IT_MATNR_LIST")
        val matNrList: List<MatNrParam>? = null
)

data class EanParam(
        @SerializedName("EAN")
        val ean: String
)

data class MatNrParam(
        @SerializedName("MATNR")
        val matNr: String
)


class ProductInfoStatus : ObjectRawStatus<ProductInfoResult>()

data class ProductInfoResult(
        @SerializedName("ET_ADDINFO")
        val additionalInfoList: List<AdditionalInfo>,
        @SerializedName("ET_LIFNR")
        val suppliers: List<Supplier>,
        @SerializedName("ET_MATERIALS")
        // Информация по товарам
        val productsInfo: List<ProductInfo>,
        @SerializedName("ET_PLACES")
        val places: List<Place>,
        @SerializedName("ET_PRICE")
        val prices: List<Price>,
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>,
        @SerializedName("ET_STOCKS")
        val stocks: List<Stock>
)


data class ProductInfo(
        //Номер отдела (секция)
        @SerializedName("ABTNR")
        val sectionNumber: String,
        //ЕИ заказа на поставку
        @SerializedName("BSTME")
        val bSTME: String,
        //Базисная единица измерения
        @SerializedName("BUOM")
        val bUom: String,
        //Глобальный номер товара (GTIN)
        @SerializedName("EAN")
        val ean: String,
        //Знаменатель при пересчете в базисные единицы измерения
        @SerializedName("EAN_UMREN")
        val eanUmRen: String,
        //Числитель для пересчета в базисные единицы измерения
        @SerializedName("EAN_UMREZ")
        val eanUmRez: String,
        @SerializedName("EAN_UOM")
        //Единица измерения
        val eanUOM: String,
        //Группа закупок
        @SerializedName("EKGRP")
        val eKGRP: String,
        //Признак - Товар алкоголь
        @SerializedName("IS_ALCO")
        val isAlco: String,
        //Признак - Акцизный алкоголь
        @SerializedName("IS_EXC")
        val isExcise: String,
        //Признак – товар здоровое питание
        @SerializedName("IS_HF")
        val isHealthyFood: String,
        //Признак – Товар маркированный
        @SerializedName("IS_MARK")
        val iSMarked: String,
        //Признак – товар новинка
        @SerializedName("IS_NEW")
        val isNew: String,
        //Признак – товар ветеринарный
        @SerializedName("IS_VET")
        val isVet: String,
        //SAP-код товара
        @SerializedName("MATERIAL")
        val matNr: String,
        //Группа товаров
        @SerializedName("MATKL")
        val matKL: String,
        //Тип матрицы SKU
        @SerializedName("MATR_TYPE")
        val matrixType: String,
        //Общий срок годности в днях
        @SerializedName("MHDHB_DAYS")
        val expirationDate: Int,
        //Остаточный срок годности в днях
        @SerializedName("MHDRZ_DAYS")
        val remainingExpirationDate: Int,
        //Длинное наименование
        @SerializedName("NAME")
        val name: String
)

data class AdditionalInfo(
        @SerializedName("MATNR")
        val matnr: String,

        @SerializedName("MIN_STOCK")
        val minStock: String,

        @SerializedName("LAST_INV")
        val lastInv: String,

        @SerializedName("PLAN_DELIVERY")
        val planDelivery: String,

        @SerializedName("PRICE1")
        val price1: String,

        @SerializedName("PRICE2")
        val price2: String,

        @SerializedName("PROMO_TEXT1")
        val promoText1: String,

        @SerializedName("PROMO_TEXT2")
        val promoText2: String

)

data class Supplier(
        @SerializedName("MATNR")
        val matnr: String,

        @SerializedName("LIFNR")
        val lifnr: String,

        @SerializedName("LIFNR_NAME")
        val lifnrName: String,

        @SerializedName("PERIOD_ACT")
        val periodAct: String
)

data class Place(
        @SerializedName("MATNR")
        var matnr: String,
        @SerializedName("PLACE_CODE")
        var placeCode: String
)

data class Price(
        @SerializedName("MATNR")
        var matnr: String,

        @SerializedName("PRICE1")
        var price1: String,

        @SerializedName("PRICE2")
        var price2: String,

        @SerializedName("PRICE3")
        var price3: String,

        @SerializedName("PRICE4")
        var price4: String


)


data class Stock(

        @SerializedName("MATNR")
        var matnr: String,

        @SerializedName("LGORT")
        var lgort: String,

        @SerializedName("STOCK")
        var stock: Double
)
