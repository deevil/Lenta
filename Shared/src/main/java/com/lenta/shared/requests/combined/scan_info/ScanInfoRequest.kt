package com.lenta.shared.requests.combined.scan_info

import com.google.gson.Gson
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.requests.combined.scan_info.pojo.*
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.ANALYTICS_HELPER
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import java.util.*

class ScanInfoRequest(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<ScanInfoResult, ScanInfoRequestParams> {

    private val eanInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) }
    private val productInfo: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) }
    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) }
    private val zmpUtz46V001: ZmpUtz46V001 by lazy { ZmpUtz46V001(hyperHive) }

    override suspend fun run(params: ScanInfoRequestParams): Either<Failure, ScanInfoResult> {

        val scanCodeInfo = ScanCodeInfo(params.number, if (params.fromScan) null else 0.0)

        if (!scanCodeInfo.isEnterCodeValid) {
            return Either.Left(Failure.NotValidEnterNumber)
        }

        val eanInfo = if (params.isBarCode == false) {
            null
        } else {
            eanInfo.getEanInfo(scanCodeInfo.eanNumberForSearch)?.toEanInfo()
        }

        Logg.d { "eanInfo $eanInfo" }

        return searchMaterialFromDb(scanCodeInfo, eanInfo)
                ?: searchMaterialFromServer(scanCodeInfo, params.tkNumber)

    }

    private fun searchMaterialFromServer(scanCodeInfo: ScanCodeInfo, tk: String): Either<Failure, ScanInfoResult> {
        Logg.d { "--> Product search from server" }

        val productInfoNetRequestParams = ProductInfoNetRequestParams(
                ean = scanCodeInfo.eanNumberForSearch.orEmpty(),
                tk = tk,
                matNr = scanCodeInfo.materialNumberForSearch.orEmpty()
        )

        Logg.d { "searchParams: $productInfoNetRequestParams" }

        "ZMP_UTZ_WOB_02_V001".let { resName ->
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
                    }, ProductInfoStatus::class.java)
                    .execute()

            ANALYTICS_HELPER?.onFinishFmpRequest(resName)

            Logg.d { "productInfoStatus: $productInfoStatus" }


            if (productInfoStatus.isNotBad()) {

                val quantity = scanCodeInfo.extractQuantityFromEan(
                        eanInfo = productInfoStatus.result!!.raw!!.ean!!.toEan()
                )

                productInfoStatus.result?.raw?.let {
                    val productInfo = it.getProductInfo(units.getUomInfo(it.material?.buom))
                    productInfo?.let { info ->
                        return Either.Right(ScanInfoResult(info, quantity))
                    }
                    return Either.Left(Failure.GoodNotFound)
                }
            }

            return Either.Left(productInfoStatus.getFailure()).apply {
                ANALYTICS_HELPER?.logRequestError(resName, productInfoStatus)
            }

        }


    }

    private fun searchMaterialFromDb(scanCodeInfo: ScanCodeInfo, eanInfo: EanInfo?): Either<Failure, ScanInfoResult>? {
        Logg.d { "--> Product search from db" }

        val quantity = scanCodeInfo.extractQuantityFromEan(eanInfo = eanInfo)

        val materialInfo = productInfo.getProductInfoByMaterial(eanInfo?.materialNumber
                ?: scanCodeInfo.materialNumberForSearch ?: return null)
                ?: return null

        return getResult(materialInfo, quantity)
    }

    private fun getResult(materialInfo: ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST, quantity: Double): Either<Failure, ScanInfoResult> {
        val uomInfo = units.getUomInfo(materialInfo.buom)
                ?: return Either.Left(Failure.GoodNotFound)

        return Either.Right(
                ScanInfoResult(
                        productInfo = ProductInfo(
                                materialNumber = materialInfo.material.orEmpty(),
                                description = materialInfo.name.orEmpty(),
                                uom = Uom(code = uomInfo.uom.orEmpty(), name = uomInfo.name?.toLowerCase(Locale.getDefault()).orEmpty()),
                                type = materialInfo.getProductType(),
                                isSet = zmpUtz46V001.isSet(materialInfo.material.orEmpty()),
                                sectionId = materialInfo.abtnr.orEmpty(),
                                matrixType = materialInfo.getMatrixType(),
                                materialType = materialInfo.matype.orEmpty(),
                                markedGoodType = materialInfo.markType.orEmpty()
                        ),
                        quantity = quantity
                )
        )
    }

    private fun ProductServerInfo?.getProductInfo(uomInfo: ZmpUtz07V001.ItemLocal_ET_UOMS?): ProductInfo? {
        if (this?.material?.material == null || uomInfo == null) {
            return null
        }

        val isMarkedGood = material.isMark.orEmpty().isNotEmpty() || material.markType.orEmpty().isNotEmpty()

        return ProductInfo(
                materialNumber = material.material,
                description = material.name.orEmpty(),
                uom = Uom(code = uomInfo.uom.orEmpty(), name = uomInfo.name?.toLowerCase(Locale.getDefault()).orEmpty()),
                type = getProductType(isAlco = material.isAlco?.isNotEmpty() == true, isExcise = material.isExcise?.isNotEmpty() == true, isMarkedGood = isMarkedGood),
                isSet = !set.isNullOrEmpty(),
                sectionId = material.abtnr.orEmpty(),
                matrixType = getMatrixType(material.matrixType.orEmpty()),
                materialType = material.materialType.orEmpty(),
                markedGoodType = material.markType.orEmpty()
        )
    }
}


data class ScanInfoResult(
        val productInfo: ProductInfo,
        val quantity: Double
)

data class ScanInfoRequestParams(
        var number: String,
        val tkNumber: String,
        val fromScan: Boolean,
        val isBarCode: Boolean? = null
)


