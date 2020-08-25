package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.repository.IngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.flatMap
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class GetEanIngredientsNetRequest @Inject constructor(
        private val ingredientsRepository: IngredientsRepository
) : UseCase<List<OrderByBarcode>,GetIngredientDataParams>{

    override suspend fun run(params: GetIngredientDataParams): Either<Failure, List<OrderByBarcode>> {
        return ingredientsRepository.getIngredientListData(params).flatMap { result ->
            Either.Right(result.orderByBarcode.orEmpty())
        }
    }
}