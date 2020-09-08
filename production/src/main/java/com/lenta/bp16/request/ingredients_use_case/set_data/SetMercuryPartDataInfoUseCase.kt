package com.lenta.bp16.request.ingredients_use_case.set_data

import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.bp16.model.ingredients.MercuryPartDataInfo
import com.lenta.shared.interactor.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetMercuryPartDataInfoUseCase @Inject constructor(
        private val ingredientDataPersistStorage: IIngredientDataPersistStorage
) : IUseCase.In<MercuryPartDataInfo> {
    override suspend fun invoke(params: List<MercuryPartDataInfo>) {
        return withContext(Dispatchers.IO) {
            ingredientDataPersistStorage.saveMercuryDataInfo(params)
        }
    }
}