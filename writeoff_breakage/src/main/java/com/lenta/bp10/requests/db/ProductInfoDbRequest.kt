package com.lenta.bp10.requests.db

import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class ProductInfoDbRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<ProductInfo, ProductInfoRequestParams>() {

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

    @Suppress("FoldInitializerAndIfToElvis")
    override suspend fun run(params: ProductInfoRequestParams): Either<Failure, ProductInfo> {

        var eanInfo = zmpUtz25V001.getEanInfo(params.number)

        Logg.d { "eanInfo.material: ${eanInfo?.material}" }


        val materialInfo = zmpUtz30V001.getMaterial(if (eanInfo != null) eanInfo.material else params.number)

        if (materialInfo == null) {
            return Either.Left(Failure.GoodNotFound)
        }

        if (eanInfo == null) {
            eanInfo = zmpUtz25V001.getEanInfoFromMaterial(materialInfo.material)
        }

        if (eanInfo == null) {
            return Either.Left(Failure.GoodNotFound)
        }


        val uomInfo = zmpUtz07V001.getUomInfo(eanInfo.uom)

        if (uomInfo == null) {
            return Either.Left(Failure.GoodNotFound)
        }

        return Either.Right(ProductInfo(
                materialNumber = materialInfo.material,
                description = materialInfo.name,
                uom = Uom(code = uomInfo.uom, name = uomInfo.name),
                type = materialInfo.getProductType(),
                isSet = getIsSet(materialInfo.material),
                sectionNumber = materialInfo.getSectionId(),
                matrixType = materialInfo.getMatrixType(),
                materialType = materialInfo.matype
        ))

    }

    private fun getIsSet(materialNumber: String): Boolean {
        return zmpUtz46V001.isSet(materialNumber)
    }

}


data class ProductInfoRequestParams(
        var number: String
)
