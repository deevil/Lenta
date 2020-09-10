package com.lenta.bp16.request.ingredients_use_case.set_data

import com.lenta.bp16.model.AddAttributeProdInfo
import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.shared.interactor.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetAddAttributeInfoUseCase @Inject constructor(
        private val ingredientDataPersistStorage: IIngredientDataPersistStorage
) : IUseCase.In<List<AddAttributeProdInfo>> {
    override suspend fun invoke(params: List<AddAttributeProdInfo>) {
        return withContext(Dispatchers.IO) {
            ingredientDataPersistStorage.saveAddAttributeInfo(params)
        }
    }
}