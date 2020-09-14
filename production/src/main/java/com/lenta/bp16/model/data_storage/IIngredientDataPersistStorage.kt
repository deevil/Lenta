package com.lenta.bp16.model.data_storage

import com.lenta.bp16.model.ingredients.ui.MercuryPartDataInfoUI
import com.lenta.bp16.model.ingredients.ui.ProducerDataInfoUI
import com.lenta.bp16.model.ingredients.ui.ZPartDataInfoUI

interface IIngredientDataPersistStorage {
    fun saveZPartDataInfo(list: List<ZPartDataInfoUI>)
    fun getZPartDataInfo(): List<ZPartDataInfoUI>

    fun saveMercuryDataInfo(list: List<MercuryPartDataInfoUI>)
    fun getMercuryDataInfo(): List<MercuryPartDataInfoUI>

    fun saveProducerDataInfo(list: List<ProducerDataInfoUI>)
    fun getProducerDataInfo(): List<ProducerDataInfoUI>

    fun saveWarehouseForItemSelected(list: List<String>)
    fun getWarehouseForItemSelected(): List<String>
}