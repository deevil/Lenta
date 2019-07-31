package com.lenta.bp7.data

import com.google.gson.Gson
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Segment
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PersistCheckResult @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IPersistCheckResult {

    private val keyForSave = "PLANOGRAM_CHECK_RESULT"

    override fun saveCheckResult(checkData: CheckData?) {
        if (checkData == null || checkData.segments.isEmpty()) {
            hyperHive.stateAPI.saveParamToDB(keyForSave, null)
        } else {
            hyperHive.stateAPI.saveParamToDB(keyForSave, gson.toJson(CheckResultData(
                    checkType = checkData.checkType,
                    countFacings = checkData.countFacings,
                    checkEmptyPlaces = checkData.checkEmptyPlaces,
                    segments = checkData.segments)))
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

}

interface IPersistCheckResult {
    fun saveCheckResult(checkData: CheckData?)
    fun getSavedCheckResult(): CheckResultData?
    fun clearSavedData()
}

data class CheckResultData(
        val checkType: CheckType,
        val countFacings: Boolean,
        val checkEmptyPlaces: Boolean,
        val segments: List<Segment>
)