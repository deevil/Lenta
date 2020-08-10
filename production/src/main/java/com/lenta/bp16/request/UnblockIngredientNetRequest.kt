package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.params.UnblockIngredientsParams
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class UnblockIngredientNetRequest @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<Boolean, UnblockIngredientsParams> {
    override suspend fun run(params: UnblockIngredientsParams): Either<Failure, Boolean> {
        return ingredientsRepository.unblockOrderIngredients(params)
    }
}