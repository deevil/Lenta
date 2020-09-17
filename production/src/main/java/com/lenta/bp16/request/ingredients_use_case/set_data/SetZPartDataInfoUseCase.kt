package com.lenta.bp16.request.ingredients_use_case.set_data

import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.bp16.model.ingredients.ui.ZPartDataInfoUI
import com.lenta.shared.interactor.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetZPartDataInfoUseCase @Inject constructor(
        private val ingredientDataPersistStorage: IIngredientDataPersistStorage
) : IUseCase.In<List<ZPartDataInfoUI>> {
    override suspend fun invoke(params: List<ZPartDataInfoUI>) {
        return withContext(Dispatchers.IO) {
            ingredientDataPersistStorage.saveZPartDataInfo(params)
        }
    }
}