package com.lenta.bp10.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.utilities.extentions.hhive.ANALYTICS_HELPER
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class ProductInfoNetRequest
@Inject constructor(private val hyperHive: HyperHive,
                    private val gson: Gson,
                    private val processServiceManager: IWriteOffTaskManager,
                    private val sessionInfo: ISessionInfo) :
        UseCase<ProductInfo, ProductInfoRequestParams> {

    private val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }


    override suspend fun run(params: ProductInfoRequestParams): Either<Failure, ProductInfo> {

        val productInfoNetRequestParams = ProductInfoNetRequestParams(
                ean = params.number,
                tk = processServiceManager.getWriteOffTask()?.taskDescription?.tkNumber.orEmpty(),
                matNr = ""
        )

        val resName = "ZMP_UTZ_WOB_02_V001"

        val productInfoStatus = hyperHive.requestAPI.web(resName,
                WebCallParams().apply {
                    data = gson.toJson(productInfoNetRequestParams)
                    headers = mapOf(
                            "X-SUP-DOMAIN" to "DM-MAIN",
                            "Content-Type" to "application/json",
                            "Web-Authorization" to sessionInfo.basicAuth
                    )
                }.apply {
                    ANALYTICS_HELPER?.onStartFmpRequest(resName, "headers: ${this.headers}, data: ${this.data}")
                },
                ProductInfoStatus::class.java)
                .execute()


        if (productInfoStatus.isNotBad()) {
            productInfoStatus.result?.raw?.let {
                val productInfo = it.getProductInfo(zmpUtz07V001.getUomInfo(it.material?.buom))
                productInfo?.let { info ->
                    return Either.Right(info)
                }
                return Either.Left(Failure.GoodNotFound)
            }
        }

        return Either.Left(productInfoStatus.getFailure())

    }

    fun ProductServerInfo?.getProductInfo(uomInfo: ZmpUtz07V001.ItemLocal_ET_UOMS?): ProductInfo? {
        if (this?.material?.material == null || uomInfo == null) {
            return null
        }

        return ProductInfo(
                materialNumber = material.material,
                description = material.name.orEmpty(),
                uom = Uom(code = uomInfo.uom.orEmpty(), name = uomInfo.name.orEmpty()),
                type = getProductType(
                        isAlco = material.isAlco?.isNotEmpty() == true,
                        isExcise = material.isExcise?.isNotEmpty() == true,
                        isMarkedGood = material.isMark?.isNotEmpty() == true
                ),
                isSet = !set.isNullOrEmpty(),
                sectionId = material.abtnr.orEmpty(),
                matrixType = getMatrixType(material.matrixType.orEmpty()),
                materialType = material.materialType.orEmpty()
        )

    }
}

data class ProductInfoNetRequestParams(
        @SerializedName("IV_EAN")
        val ean: String,
        @SerializedName("IV_WERKS")
        val tk: String,
        @SerializedName("IV_MATNR")
        val matNr: String
)

class ProductInfoStatus : ObjectRawStatus<ProductServerInfo>()


data class ProductServerInfo(
        @SerializedName("ES_EAN")
        val ean: Ean?,
        @SerializedName("ES_MATERIAL")
        val material: Material?,
        @SerializedName("ET_SET")
        val set: List<Set>?,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String?,
        @SerializedName("EV_RETCODE")
        val retCode: Int?
)

data class Material(
        @SerializedName("ABTNR")
        val abtnr: String?,
        @SerializedName("BUOM")
        val buom: String?,
        @SerializedName("EKGRP")
        val ekgrp: String?,
        @SerializedName("IS_ALCO")
        val isAlco: String?,
        @SerializedName("IS_EXC")
        val isExcise: String?,
        @SerializedName("IS_MARK")
        val isMark: String?,
        @SerializedName("IS_RETURN")
        val isReturn: String?,
        @SerializedName("MATERIAL")
        val material: String?,
        @SerializedName("MATKL")
        val matkl: String?,
        @SerializedName("MATR_TYPE")
        val matrixType: String?,
        @SerializedName("MATYPE")
        val materialType: String?,
        @SerializedName("NAME")
        val name: String?
)

data class Ean(
        @SerializedName("EAN")
        val ean: String?,
        @SerializedName("MATERIAL")
        val materialNumber: String?,
        @SerializedName("UMREN")
        val umren: String?,
        @SerializedName("UMREZ")
        val umrez: String?,
        @SerializedName("UOM")
        val uom: String?
)

data class Set(
        @SerializedName("MATNR")
        val matNr: String?,
        @SerializedName("MATNR_OSN")
        val matNrOsn: String?,
        @SerializedName("MEINS")
        val meins: String?,
        @SerializedName("MENGE")
        val menge: String?
)



