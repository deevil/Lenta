package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.GoodByOrder
import com.lenta.bp16.model.ingredients.params.GetIngredientsParams
import com.lenta.bp16.repository.IngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class GetGoodsByOrderNetRequest @Inject constructor(
        private val ingredientsRepository: IngredientsRepository
) : UseCase<List<GoodByOrder>, GetIngredientsParams> {
    override suspend fun run(params: GetIngredientsParams): Either<Failure, List<GoodByOrder>> {
        return ingredientsRepository.getGoodsByOrder(params)
    }
}