package com.lenta.bp16.request.ingredients_use_case.get_data

import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.bp16.model.ingredients.ui.ZPartDataInfoUI
import com.lenta.shared.interactor.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetZPartDataInfoUseCase @Inject constructor(
        private val ingredientDataPersistStorage: IIngredientDataPersistStorage
) : IUseCase.Out<List<ZPartDataInfoUI>> {
    override suspend fun invoke(): List<ZPartDataInfoUI> {
        return withContext(Dispatchers.IO) {
            ingredientDataPersistStorage.getZPartDataInfo()
        }
    }
}