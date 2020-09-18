package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.ui.IngredientsDataListResultUI
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.flatMap
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class GetIngredientsDataListNetRequest @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<IngredientsDataListResultUI, GetIngredientDataParams> {
    override suspend fun run(params: GetIngredientDataParams): Either<Failure, IngredientsDataListResultUI> {
        return ingredientsRepository.getIngredientListData(params).flatMap { Either.Right(it.convert()) }
    }
}