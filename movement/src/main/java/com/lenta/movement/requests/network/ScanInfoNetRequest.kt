package com.lenta.movement.requests.network

import com.lenta.movement.models.ProductInfo
import com.lenta.movement.requests.network.models.scanInfoNetRequest.ScanInfoParams
import com.lenta.movement.requests.network.models.scanInfoNetRequest.ScanInfoResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
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
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = ScanInfoStatus::class.java
        )

        return if (result is Either.Left) {
            result
        } else {
            val status = (result as Either.Right).b

            val productInfo = getProductInfo(status, units.getUomInfo(status.material?.buom))

            productInfo?.let(result::right) ?: Either.Left(Failure.GoodNotFound)
        }
    }

    private fun getProductInfo(status: ScanInfoResult, uomInfo: ZmpUtz07V001.ItemLocal_ET_UOMS?): ProductInfo? {
        val material = status.material
        val set = status.set
        val suppliers = status.suppliers

        return material?.let { product ->
            product.material?.let { productNumber ->
                uomInfo?.let { actualUom ->
                    ProductInfo(
                            materialNumber = productNumber,
                            description = product.name.orEmpty(),
                            uom = Uom(code = actualUom.uom.orEmpty(), name = actualUom.name.orEmpty()),
                            type = getProductType(
                                    isAlco = product.isAlco?.isNotEmpty() ?: false,
                                    isExcise = product.isExcise?.isNotEmpty() ?: false,
                                    isMarkedGood = product.markType?.isNotEmpty() ?: false,
                                    isVet = product.isVet?.isNotEmpty() ?: false
                            ),
                            isSet = !set.isNullOrEmpty(),
                            sectionId = product.abtnr.orEmpty(),
                            matrixType = getMatrixType(product.matrixType.orEmpty()),
                            materialType = product.materialType.orEmpty(),
                            suppliers = suppliers.orEmpty(),
                            ekGroup = product.ekgrp.orEmpty(),
                            matkl = product.matkl.orEmpty(),
                            volume = product.volume?.toDoubleOrNull() ?: 0.0,
                            quantityInvestments = product.quantityInvestments?.toDoubleOrNull()?.toInt()
                                    ?: 0,
                            isRus = product.isRus.isSapTrue(),
                            isVet = product.isVet.isSapTrue(),
                            isFood = product.isFood.isSapTrue()
                    )
                }
            }
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_BKS_05_V001"
    }
}

class ScanInfoStatus : ObjectRawStatus<ScanInfoResult>()

