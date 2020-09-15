package com.lenta.bp16.model.data_storage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lenta.bp16.model.AddAttributeProdInfo
import com.lenta.bp16.model.ingredients.ui.MercuryPartDataInfoUI
import com.lenta.bp16.model.ingredients.ui.ProducerDataInfoUI
import com.lenta.bp16.model.ingredients.ui.ZPartDataInfoUI
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class IngredientDataPersistStorage @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IIngredientDataPersistStorage {
    override fun saveZPartDataInfo(list: List<ZPartDataInfoUI>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_ZPART_DATA_INFO,
                gson.toJson(list)
        )
    }

    override fun getZPartDataInfo(): List<ZPartDataInfoUI> {
        return hyperHive.stateAPI.getParamFromDB(KEY_ZPART_DATA_INFO)?.let { json ->
            val type = object : TypeToken<List<ZPartDataInfoUI>>() {}.type
            gson.fromJson(json, type) as? List<ZPartDataInfoUI>
        }.orEmpty()
    }

    override fun saveMercuryDataInfo(list: List<MercuryPartDataInfoUI>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_MERCURY_DATA_INFO,
                gson.toJson(list)
        )
    }

    override fun getMercuryDataInfo(): List<MercuryPartDataInfoUI> {
        return hyperHive.stateAPI.getParamFromDB(KEY_MERCURY_DATA_INFO)?.let { json ->
            val type = object : TypeToken<List<MercuryPartDataInfoUI>>() {}.type
            gson.fromJson(json, type) as? List<MercuryPartDataInfoUI>
        }.orEmpty()
    }

    override fun saveProducerDataInfo(list: List<ProducerDataInfoUI>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_PRODUCER_DATA_INFO,
                gson.toJson(list)
        )
    }

    override fun getProducerDataInfo(): List<ProducerDataInfoUI> {
        return hyperHive.stateAPI.getParamFromDB(KEY_PRODUCER_DATA_INFO)?.let { json ->
            val type = object : TypeToken<List<ProducerDataInfoUI>>() {}.type
            gson.fromJson(json, type) as? List<ProducerDataInfoUI>
        }.orEmpty()
    }

    override fun saveWarehouseForItemSelected(list: List<String>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_WAREHOUSE_FOR_ITEM_SELECTED,
                gson.toJson(list)
        )
    }

    override fun getWarehouseForItemSelected(): List<String> {
        return hyperHive.stateAPI.getParamFromDB(KEY_WAREHOUSE_FOR_ITEM_SELECTED)?.let { json ->
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) as? List<String>
        }.orEmpty()
    }

    companion object {
        private const val KEY_PRODUCER_DATA_INFO = "KEY_PRODUCER_DATA_INFO"
        private const val KEY_ZPART_DATA_INFO = "KEY_ZPART_DATA_INFO"
        private const val KEY_MERCURY_DATA_INFO = "KEY_MERCURY_DATA_INFO"
        private const val KEY_WAREHOUSE_FOR_ITEM_SELECTED = "KEY_WAREHOUSE_FOR_ITEM_SELECTED"
    }
}