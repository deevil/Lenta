package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.params.MaterialDataCompleteParams
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class CompleteIngredientByMaterialNetRequest @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<Boolean, MaterialDataCompleteParams> {
    override suspend fun run(params: MaterialDataCompleteParams): Either<Failure, Boolean> {
        return ingredientsRepository.completeToPackMaterialData(params)
    }
}