package com.lenta.bp16.request.ingredients_use_case.set_data

import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.shared.interactor.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetProducerDataInfoUseCase @Inject constructor(
        private val ingredientDataPersistStorage: IIngredientDataPersistStorage
): IUseCase.In<List<ProducerDataInfo>> {
    override suspend fun invoke(params: List<ProducerDataInfo>) {
        return withContext(Dispatchers.IO){
            ingredientDataPersistStorage.saveProducerDataInfo(params)
        }
    }
}