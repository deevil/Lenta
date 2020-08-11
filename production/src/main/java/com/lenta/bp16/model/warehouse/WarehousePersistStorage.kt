package com.lenta.bp16.model.warehouse

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lenta.bp16.model.DataForSave
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class WarehousePersistStorage @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IWarehousePersistStorage {

    override fun saveSelectedWarehouses(list: Set<String>) {
        hyperHive.stateAPI.saveParamToDB(
                KEY_TO_SELECTED_WAREHOUSE,
                gson.toJson(list)
        )
    }

    override fun getSelectedWarehouses(): Set<String> {
        return hyperHive.stateAPI.getParamFromDB(KEY_TO_SELECTED_WAREHOUSE)?.let { json ->
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson(json, type) as? Set<String>
        } ?: emptySet()
    }

    companion object {
        private const val KEY_TO_SELECTED_WAREHOUSE = "KEY_TO_SELECTED_WAREHOUSE"
    }
}