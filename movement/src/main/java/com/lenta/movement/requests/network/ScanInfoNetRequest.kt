package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.models.ProductInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Supplier
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.isSapTrue
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class ScanInfoNetRequest @Inject constructor(
    private val hyperHive: HyperHive,
    private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<ProductInfo, ScanInfoParams> {

    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) }

    override suspend fun run(params: ScanInfoParams): Either<Failure, ProductInfo> {
        val result = fmpRequestsHelper.restRequest(
            resourceName = "ZMP_UTZ_BKS_05_V001",
            data = params,
            clazz = ScanInfoStatus::class.java
        )

        return if (result is Either.Left) {
            result
        } else {
            val status = (result as Either.Right).b
            val productInfo = status.getProductInfo(units.getUomInfo(status.material?.buom))

            if (productInfo != null) {
                result.right(productInfo)
            } else {
                Either.Left(Failure.GoodNotFound)
            }

        }
    }

}

private fun ScanInfoResult.getProductInfo(uomInfo: ZmpUtz07V001.ItemLocal_ET_UOMS?): ProductInfo? {
    if (this.material?.material == null || uomInfo == null) {
        return null
    }

    return ProductInfo(
        materialNumber = material.material,
        description = material.name,
        uom = Uom(code = uomInfo.uom, name = uomInfo.name),
        type = getProductType(
            isAlco = material.isAlco.isNotEmpty(),
            isExcise = material.isExcise.isNotEmpty()
        ),
        isSet = !set.isNullOrEmpty(),
        sectionId = material.abtnr,
        matrixType = getMatrixType(material.matrixType),
        materialType = material.materialType,
        suppliers = suppliers,
        ekGroup = material.ekgrp,
        matkl = material.matkl,
        volume = material.volume.toDoubleOrNull() ?: 0.0,
        quantityInvestments = material.quantityInvestments.toDoubleOrNull()?.toInt() ?: 0,
        isRus = material.isRus.isSapTrue(),
        isVet = material.isVet.isSapTrue(),
        isFood = material.isFood.isSapTrue()
    )

}

data class ScanInfoParams(
    @SerializedName("IV_EAN")
    val ean: String,
    @SerializedName("IV_WERKS")
    val tk: String,
    @SerializedName("IV_MATNR")
    val matNr: String,
    @SerializedName("IV_CODEBP")
    val codeEBP: String
)

class ScanInfoStatus : ObjectRawStatus<ScanInfoResult>()

data class ScanInfoResult(
    @SerializedName("ES_EAN")
    val ean: Ean?,
    @SerializedName("ES_MATERIAL")
    val material: Material?,
    @SerializedName("ET_LIFNR")
    val suppliers: List<Supplier>,
    @SerializedName("ET_SET")
    val set: List<Set>?,
    @SerializedName("EV_ERROR_TEXT")
    val errorText: String,
    @SerializedName("EV_RETCODE")
    val retCode: Int
)

data class Material(
    @SerializedName("ABTNR")
    val abtnr: String,
    @SerializedName("BUOM")
    val buom: String,
    @SerializedName("EKGRP")
    val ekgrp: String,
    @SerializedName("MATERIAL")
    val material: String,
    @SerializedName("MATKL")
    val matkl: String,
    @SerializedName("MATR_TYPE")
    val matrixType: String,
    @SerializedName("MATYPE")
    val materialType: String,
    @SerializedName("NAME")
    val name: String,
    @SerializedName("QNTINCL")
    val quantityInvestments: String,
    @SerializedName("VOLUM")
    val volume: String,
    @SerializedName("IS_RUS")
    val isRus: String,
    @SerializedName("IS_ALCO")
    val isAlco: String,
    @SerializedName("IS_EXC")
    val isExcise: String,
    @SerializedName("IS_RETURN")
    val isReturn: String,
    @SerializedName("IS_VET")
    val isVet: String,
    @SerializedName("IS_FOOD")
    val isFood: String
)

data class Ean(
    @SerializedName("EAN")
    val ean: String,
    @SerializedName("MATERIAL")
    val materialNumber: String,
    @SerializedName("UMREN")
    val umren: Int,
    @SerializedName("UMREZ")
    val umrez: Int,
    @SerializedName("UOM")
    val uom: String
)

data class Set(
    @SerializedName("MATNR")
    val matNr: String,
    @SerializedName("MATNR_OSN")
    val matNrOsn: String,
    @SerializedName("MEINS")
    val meins: String,
    @SerializedName("MENGE")
    val menge: String
)