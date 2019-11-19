package com.lenta.bp16.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.Good
import com.lenta.shared.models.core.Uom
import javax.inject.Inject

class TaskManager  @Inject constructor() : ITaskManager {

    override fun getCurrentGood(): MutableLiveData<Good> {
        return MutableLiveData(Good(
                material = "000000000000002365",
                name = "Большая рыба",
                units = Uom.KG,
                planned = 25.0
        ))
    }

}

interface ITaskManager {

    fun getCurrentGood(): MutableLiveData<Good>

}