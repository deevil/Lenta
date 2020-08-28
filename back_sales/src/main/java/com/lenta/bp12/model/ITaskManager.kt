package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestResult

interface IMarkManager {

    val tempMarks: MutableLiveData<List<Mark>>

    fun addMarksFromResult(result: MarkCartonBoxGoodInfoNetRequestResult)
}

class MarkManager() : IMarkManager {
    override val tempMarks: MutableLiveData<List<Mark>> = MutableLiveData(mutableListOf())

    override fun addMarksFromResult(result: MarkCartonBoxGoodInfoNetRequestResult) {
        TODO("Not yet implemented")
    }

}