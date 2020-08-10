package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.TechOrderDataInfo
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class GetTechOrdersUseCase @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<List<TechOrderDataInfo>, Unit> {
    override suspend fun run(params: Unit): Either<Failure, List<TechOrderDataInfo>> {
        return Either.Right(ingredientsRepository.getTechOrdersByRemake())
    }
}
