package com.lenta.shared.requests.combined.scan_info

import com.google.gson.Gson
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.requests.combined.scan_info.pojo.*
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

@AppScope
class ScanInfoRequest @Inject constructor(private val hyperHive: HyperHive, private val gson: Gson) : UseCase<ScanInfoResult, ScanInfoRequestParams>() {

    private val zmpUtz25V001: ZmpUtz25V001 by lazy {
        ZmpUtz25V001(hyperHive)
    }

    private val zmpUtz30V001: ZmpUtz30V001 by lazy {
        ZmpUtz30V001(hyperHive)
    }

    private val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }

    private val zmpUtz46V001: ZmpUtz46V001 by lazy {
        ZmpUtz46V001(hyperHive)
    }

    override suspend fun run(params: ScanInfoRequestParams): Either<Failure, ScanInfoResult> {

        val scanCodeInfo = ScanCodeInfo(params.number, if (params.fromScan) null else 0.0)

        if (!scanCodeInfo.isEnterCodeValid) {
            return Either.Left(Failure.NotValidEnterNumber)
        }

        var eanInfo = zmpUtz25V001.getEanInfo(scanCodeInfo.eanNumberForSearch)?.toEanInfo()

        return if (eanInfo != null && params.isBarCode != true) {
            searchMaterialFromDb(scanCodeInfo, eanInfo)
                    ?: searchMaterialFromServer(scanCodeInfo, params.tkNumber)
        } else {
            val materialInfo = if (params.isBarCode == true) {
                return Either.Left(Failure.GoodNotFound)
            } else {
                zmpUtz30V001.getMaterial(params.number)
            }
            if (materialInfo != null) {
                eanInfo = zmpUtz25V001.getEanInfoFromMaterial(materialInfo.material)?.toEanInfo()
                getResult(materialInfo, scanCodeInfo.extractQuantityFromEan(eanInfo))
            } else {
                if (params.isBarCode == true) {
                    Either.Left(Failure.GoodNotFound)
                } else {
                    searchMaterialFromServer(scanCodeInfo, params.tkNumber)
                }
            }
        }
    }

    private fun searchMaterialFromServer(scanCodeInfo: ScanCodeInfo, tk: String): Either<Failure, ScanInfoResult> {
        val productInfoNetRequestParams = ProductInfoNetRequestParams(
                ean = scanCodeInfo.eanNumberForSearch ?: "",
                tk = tk,
                matNr = scanCodeInfo.materialNumberForSearch ?: ""
        )

        Logg.d { "searchParams: $productInfoNetRequestParams" }

        val stringRes = hyperHive.requestAPI.web("ZMP_UTZ_WOB_02_V001",
                WebCallParams().apply {
                    data = gson.toJson(productInfoNetRequestParams)
                    headers = mapOf(
                            "X-SUP-DOMAIN" to "DM-MAIN",
                            "Content-Type" to "application/json"
                    )
                })
                .execute()

        Logg.d { "searchRes: $stringRes" }

        val productInfoStatus: ProductInfoStatus = gson.fromJson(stringRes, ProductInfoStatus::class.java)

        if (productInfoStatus.isNotBad()) {

            val quantity = scanCodeInfo.extractQuantityFromEan(
                    eanInfo = productInfoStatus.result!!.raw!!.ean!!.toEan()
            )

            productInfoStatus.result?.raw?.let {
                val productInfo = it.getProductInfo(zmpUtz07V001.getUomInfo(it.material?.buom))
                productInfo?.let { info ->
                    return Either.Right(ScanInfoResult(info, quantity))
                }
                return Either.Left(Failure.GoodNotFound)
            }
        }

        return Either.Left(productInfoStatus.getFailure())

    }

    private fun searchMaterialFromDb(scanCodeInfo: ScanCodeInfo, eanInfo: EanInfo): Either<Failure, ScanInfoResult>? {

        val quantity = scanCodeInfo.extractQuantityFromEan(eanInfo = eanInfo)

        val materialInfo = zmpUtz30V001.getMaterial(eanInfo.materialNumber)
                ?: return null

        return getResult(materialInfo, quantity)
    }

    private fun getResult(materialInfo: ZmpUtz30V001.ItemLocal_ET_MATERIALS, quantity: Double): Either<Failure, ScanInfoResult> {
        val uomInfo = zmpUtz07V001.getUomInfo(materialInfo.buom)
                ?: return Either.Left(Failure.GoodNotFound)

        return Either.Right(
                ScanInfoResult(
                        productInfo = ProductInfo(
                                materialNumber = materialInfo.material,
                                description = materialInfo.name,
                                uom = Uom(code = uomInfo.uom, name = uomInfo.name),
                                type = materialInfo.getProductType(),
                                isSet = zmpUtz46V001.isSet(materialInfo.material),
                                sectionId = materialInfo.abtnr,
                                matrixType = materialInfo.getMatrixType(),
                                materialType = materialInfo.matype
                        ),
                        quantity = quantity
                )
        )
    }

    private fun ProductServerInfo?.getProductInfo(uomInfo: ZmpUtz07V001.ItemLocal_ET_UOMS?): ProductInfo? {
        if (this?.material?.material == null || uomInfo == null) {
            return null
        }

        return ProductInfo(
                materialNumber = material.material,
                description = material.name,
                uom = Uom(code = uomInfo.uom, name = uomInfo.name),
                type = getProductType(isAlco = material.isAlco.isNotEmpty(), isExcise = material.isExcise.isNotEmpty()),
                isSet = !set.isNullOrEmpty(),
                sectionId = material.abtnr,
                matrixType = getMatrixType(material.matrixType),
                materialType = material.materialType
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


