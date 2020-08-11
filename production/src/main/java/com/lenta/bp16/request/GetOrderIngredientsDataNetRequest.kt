package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.flatMap
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class GetOrderIngredientsDataNetRequest @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<List<OrderIngredientDataInfo>, GetIngredientDataParams>{
    override suspend fun run(params: GetIngredientDataParams): Either<Failure, List<OrderIngredientDataInfo>> {
        return ingredientsRepository.getIngredientListData(params).flatMap { result ->
            Either.Right(result.ordersIngredientsDataInfoList.orEmpty())
        }
    }
}
