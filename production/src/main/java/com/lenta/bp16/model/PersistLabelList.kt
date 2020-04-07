package com.lenta.bp16.model

import com.google.gson.Gson
import com.lenta.bp16.data.LabelInfo
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PersistLabelList @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IPersistLabelList {

    private val keyForSave = "PRO_LABEL_LIST"

    override fun saveLabelList(labels: List<LabelInfo>) {
        hyperHive.stateAPI.saveParamToDB(keyForSave, gson.toJson(
                DataForSave(
                        labels = labels
                )
        ))
    }

    override fun getLabelList(): List<LabelInfo> {
        hyperHive.stateAPI.getParamFromDB(keyForSave)?.let { json ->
            return gson.fromJson(json, DataForSave::class.java).labels
        }
        return emptyList()
    }

    override fun clearSavedData() {
        hyperHive.stateAPI.saveParamToDB(keyForSave, null)
    }

}

interface IPersistLabelList {
    fun saveLabelList(labels: List<LabelInfo>)
    fun getLabelList(): List<LabelInfo>
    fun clearSavedData()
}

data class DataForSave(
        val labels: List<LabelInfo>
)