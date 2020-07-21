package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.params.GetIngredientsParams
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class GetIngredientsNetRequest @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<List<IngredientInfo>, GetIngredientsParams>{
    override suspend fun run(params: GetIngredientsParams): Either<Failure, List<IngredientInfo>> {
        return ingredientsRepository.getAllIngredients(params)
    }
}
