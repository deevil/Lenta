package com.lenta.bp16.repository

import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.params.GetIngredientsParams
import com.lenta.bp16.model.ingredients.results.IngredientsDataListResult
import com.lenta.bp16.model.ingredients.results.IngredientsListResult
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.flatMap
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class IngredientsRepository @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : IIngredientsRepository {

    private val allIngredients: MutableList<IngredientInfo> = mutableListOf()

    override suspend fun getAllIngredients(params: GetIngredientsParams): Either<Failure, List<IngredientInfo>> {
        val result = fmpRequestsHelper.restRequest(FMP_ORDERS_RESOURCE_NAME, params, IngredientsListStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            result.retCodes.firstOrNull { retCode ->
                                retCode.retCode == 1
                            }?.let { retCode ->
                                return@rightToLeft Failure.SapError(retCode.errorText)
                            }
                        }
                )

        return result.flatMap {
            allIngredients.clear()
            allIngredients.addAll(it.ingredientsList)
            Either.Right(allIngredients)
        }
    }

    override suspend fun getIngredientListData(params: GetIngredientDataParams): Either<Failure, IngredientsDataListResult> {
        val result = fmpRequestsHelper.restRequest(FMP_ORDERS_DATA_RESOURCE_NAME, params, IngredientsDataListStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            result.retCodes.firstOrNull { retCode ->
                                retCode.retCode == 1
                            }?.let { retCode ->
                                return@rightToLeft Failure.SapError(retCode.errorText)
                            }
                        }
                )

        return result
    }

    companion object {
        private const val FMP_ORDERS_RESOURCE_NAME = "ZMP_UTZ_PRO_10_V001"
        private const val FMP_ORDERS_DATA_RESOURCE_NAME = "ZMP_UTZ_PRO_11_V001"
    }

    class IngredientsListStatus : ObjectRawStatus<IngredientsListResult>()
    class IngredientsDataListStatus : ObjectRawStatus<IngredientsDataListResult>()
}

interface IIngredientsRepository {
    suspend fun getAllIngredients(params: GetIngredientsParams): Either<Failure, List<IngredientInfo>>

    suspend fun getIngredientListData(params: GetIngredientDataParams): Either<Failure, IngredientsDataListResult>
}