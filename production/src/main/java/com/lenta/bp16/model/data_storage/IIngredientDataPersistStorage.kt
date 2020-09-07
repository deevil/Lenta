package com.lenta.bp16.model.data_storage

import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ZPartDataInfo
import com.lenta.bp16.model.ingredients.MercuryPartDataInfo

interface IIngredientDataPersistStorage {
    fun saveZPartDataInfo(list: List<ZPartDataInfo>)
    fun getZPartDataInfo(): List<ZPartDataInfo>

    fun saveMercuryDataInfo(list: List<MercuryPartDataInfo>)
    fun getMercuryDataInfo(): List<MercuryPartDataInfo>

    fun saveProducerDataInfo(list: List<ProducerDataInfo>)
    fun getProducerDataInfo(): List<ProducerDataInfo>
}