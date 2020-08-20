package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.params.GetIngredientsParams
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.flatMap
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class GetIngredientsEanInfoNetRequest @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<List<OrderByBarcode>, GetIngredientsParams> {
    override suspend fun run(params: GetIngredientsParams): Either<Failure, List<OrderByBarcodeUI>> {
        return ingredientsRepository.getIngredientEanInfo(params).flatMap {
            Either.Right(it.mapNotNull { it.convert() })
        }
    }
}