package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.params.IngredientDataCompleteParams
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class PackIngredientNetRequest @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<Boolean, IngredientDataCompleteParams> {
    override suspend fun run(params: IngredientDataCompleteParams): Either<Failure, Boolean> {
        return ingredientsRepository.completeToPackIngredientData(params)
    }
}