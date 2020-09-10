package com.lenta.bp16.request.ingredients_use_case.get_data

import com.lenta.bp16.model.AddAttributeProdInfo
import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.shared.interactor.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAddAttributeInfoUseCase @Inject constructor(
        private val ingredientDataPersistStorage: IIngredientDataPersistStorage
) : IUseCase.Out<List<AddAttributeProdInfo>> {
    override suspend fun invoke(): List<AddAttributeProdInfo> {
        return withContext(Dispatchers.IO) {
            val kek = ingredientDataPersistStorage.getAddAttributeInfo()
            kek
        }
    }
}