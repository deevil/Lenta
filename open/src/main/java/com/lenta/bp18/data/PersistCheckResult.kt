package com.lenta.bp18.data

import com.google.gson.Gson
import com.lenta.bp18.data.model.CheckData
import com.lenta.bp18.data.model.CheckResultData
import com.lenta.bp18.platform.Constants
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PersistCheckResult @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IPersistCheckResult {

    override fun saveCheckResult(checkData: CheckData?) {
        if (checkData == null || checkData.ean.isEmpty()) {
            hyperHive.stateAPI.saveParamToDB(keyForSave, null)
        } else {
            hyperHive.stateAPI.saveParamToDB(keyForSave, gson.toJson(CheckResultData(
                    good = checkData.good
            )))
        }
    }

    override fun getSavedCheckResult(): CheckResultData? {
        hyperHive.stateAPI.getParamFromDB(keyForSave)?.let { json ->
            return gson.fromJson(json, CheckResultData::class.java)
        }
        return null
    }

    override fun clearSavedData() {
        saveCheckResult(null)
    }

    companion object {
        private const val keyForSave = Constants.KEY_FOR_SAVE
    }

}

interface IPersistCheckResult {
    fun saveCheckResult(checkData: CheckData?)
    fun getSavedCheckResult(): CheckResultData?
    fun clearSavedData()
}