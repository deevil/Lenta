package com.lenta.bp16.repository

import com.lenta.bp16.model.ingredients.GetIngredientsParams
import com.lenta.bp16.model.ingredients.IngredientsListResult
import com.lenta.bp16.request.pojo.IngredientInfo
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
        val result = fmpRequestsHelper.restRequest(FMP_RESOURCE_NAME, params, IngredientsListStatus::class.java)
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

    companion object {
        private const val FMP_RESOURCE_NAME = "ZMP_UTZ_PRO_10_V001"
    }

    class IngredientsListStatus : ObjectRawStatus<IngredientsListResult>()
}

interface IIngredientsRepository {
    suspend fun getAllIngredients(params: GetIngredientsParams): Either<Failure, List<IngredientInfo>>
}