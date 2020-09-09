package com.lenta.bp16.model.data_storage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lenta.bp16.model.AddAttributeInfo
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ZPartDataInfo
import com.lenta.bp16.model.ingredients.MercuryPartDataInfo
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class IngredientDataPersistStorage @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IIngredientDataPersistStorage {
    override fun saveZPartDataInfo(list: List<ZPartDataInfo>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_ZPART_DATA_INFO,
                gson.toJson(list)
        )
    }

    override fun getZPartDataInfo(): List<ZPartDataInfo> {
        return hyperHive.stateAPI.getParamFromDB(KEY_ZPART_DATA_INFO)?.let { json ->
            val type = object : TypeToken<List<ZPartDataInfo>>() {}.type
            gson.fromJson(json, type) as? List<ZPartDataInfo>
        } ?: emptyList()
    }

    override fun saveMercuryDataInfo(list: List<MercuryPartDataInfo>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_MERCURY_DATA_INFO,
                gson.toJson(list)
        )
    }

    override fun getMercuryDataInfo(): List<MercuryPartDataInfo> {
        return hyperHive.stateAPI.getParamFromDB(KEY_MERCURY_DATA_INFO)?.let { json ->
            val type = object : TypeToken<List<MercuryPartDataInfo>>() {}.type
            gson.fromJson(json, type) as? List<MercuryPartDataInfo>
        } ?: emptyList()
    }

    override fun saveProducerDataInfo(list: List<ProducerDataInfo>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_PRODUCER_DATA_INFO,
                gson.toJson(list)
        )
    }

    override fun getProducerDataInfo(): List<ProducerDataInfo> {
        return hyperHive.stateAPI.getParamFromDB(KEY_PRODUCER_DATA_INFO)?.let { json ->
            val type = object : TypeToken<List<ProducerDataInfo>>() {}.type
            gson.fromJson(json, type) as? List<ProducerDataInfo>
        } ?: emptyList()
    }

    override fun saveAddAttributeInfo(list: List<AddAttributeInfo>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_ADD_ATTRIBUTE_INFO,
                gson.toJson(list)
        )
    }

    override fun getAddAttributeInfo(): List<AddAttributeInfo> {
        return hyperHive.stateAPI.getParamFromDB(KEY_ADD_ATTRIBUTE_INFO)?.let { json ->
            val type = object : TypeToken<List<AddAttributeInfo>>() {}.type
            gson.fromJson(json, type) as? List<AddAttributeInfo>
        } ?: emptyList()
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
        } ?: emptyList()
    }

    companion object {
        private const val KEY_PRODUCER_DATA_INFO = "KEY_PRODUCER_DATA_INFO"
        private const val KEY_ZPART_DATA_INFO = "KEY_ZPART_DATA_INFO"
        private const val KEY_MERCURY_DATA_INFO = "KEY_MERCURY_DATA_INFO"
        private const val KEY_ADD_ATTRIBUTE_INFO = "KEY_ADD_ATTRIBUTE_INFO"
        private const val KEY_WAREHOUSE_FOR_ITEM_SELECTED = "KEY_WAREHOUSE_FOR_ITEM_SELECTED"
    }
}