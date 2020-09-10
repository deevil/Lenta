package com.lenta.bp16.request.ingredients_use_case.set_data

import com.lenta.bp16.model.ZPartDataInfo
import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.shared.interactor.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetZPartDataInfoUseCase @Inject constructor(
        private val ingredientDataPersistStorage: IIngredientDataPersistStorage
) : IUseCase.In<List<ZPartDataInfo>> {
    override suspend fun invoke(params: List<ZPartDataInfo>) {
        return withContext(Dispatchers.IO) {
            ingredientDataPersistStorage.saveZPartDataInfo(params)
        }
    }
}