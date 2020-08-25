package com.lenta.bp16.request

import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientsParams
import com.lenta.bp16.model.ingredients.results.IngredientsListResult
import com.lenta.bp16.model.ingredients.ui.IngredientsListResultUI
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class GetIngredientsNetRequest @Inject constructor(
        private val ingredientsRepository: IIngredientsRepository
) : UseCase<IngredientsListResultUI, GetIngredientsParams>{
    override suspend fun run(params: GetIngredientsParams): Either<Failure, IngredientsListResultUI> {
        return ingredientsRepository.getAllIngredients(params)
    }
}