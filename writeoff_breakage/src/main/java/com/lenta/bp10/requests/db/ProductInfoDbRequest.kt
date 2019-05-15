package com.lenta.bp10.requests.db

import com.lenta.bp10.fmp.resources.dao_ext.*
import com.lenta.bp10.fmp.resources.fast.ZmpUtz07V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz25V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class ProductInfoDbRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<ProductInfo, ProductInfoRequestParams>() {

    val zmpUtz25V001: ZmpUtz25V001 by lazy {
        ZmpUtz25V001(hyperHive)
    }

    val zmpUtz30V001: ZmpUtz30V001 by lazy {
        ZmpUtz30V001(hyperHive)
    }

    val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
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
            eanInfo = getProductInfoFromRest()
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
                isSet = getIsSet(materialInfo),
                sectionNumber = materialInfo.getSectionId(),
                matrixType = materialInfo.getMatrixType(),
                materialType = materialInfo.matype
        ))

    }
//TODO (DB) Реализовать поиск через REST


    private fun getIsSet(materialInfo: ZmpUtz30V001.ItemLocal_ET_MATERIALS): Boolean {
        //TODO (DB) Реализовать после уточнения
        return false
    }



    private fun getProductInfoFromRest(): ZmpUtz25V001.ItemLocal_ET_EANS? {
        //TODO (DB) нужно реализовать получение товара через рест
        return null
    }


}


data class ProductInfoRequestParams(
        var number: String
)
