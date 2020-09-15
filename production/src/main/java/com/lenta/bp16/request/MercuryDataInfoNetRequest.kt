package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.params.MercuryDataInfoParams
import com.lenta.bp16.model.ingredients.results.MercuryDataInfoResult
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class MercuryDataInfoNetRequest @Inject constructor(
        private val ingredientRepository: IIngredientsRepository
): UseCase<MercuryDataInfoResult, MercuryDataInfoParams> {

    override suspend fun run(params: MercuryDataInfoParams): Either<Failure, MercuryDataInfoResult> {
        return ingredientRepository.getMercuryDataInfo(params)
    }
}