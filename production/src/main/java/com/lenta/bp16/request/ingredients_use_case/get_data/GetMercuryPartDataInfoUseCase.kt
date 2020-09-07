package com.lenta.bp16.request.ingredients_use_case.get_data

import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.bp16.model.ingredients.MercuryPartDataInfo
import com.lenta.shared.interactor.IUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetMercuryPartDataInfoUseCase @Inject constructor(
        private val ingredientDataPersistStorage: IIngredientDataPersistStorage
) : IUseCase.Out<List<MercuryPartDataInfo>> {
    override suspend fun invoke(): List<MercuryPartDataInfo> {
        return withContext(Dispatchers.IO){
            ingredientDataPersistStorage.getMercuryDataInfo()
        }
    }
}