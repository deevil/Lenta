package com.lenta.bp12.model.pojo

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.model.pojo.create_task.TaskCreate

interface TaskManager {

    var searchNumber: String
    var isSearchFromList: Boolean
    var isWasAddedProvider: Boolean
    var isWholesaleTaskType: Boolean
    var isBasketsNeedsToBeClosed: Boolean

    val currentTask: MutableLiveData<TaskCreate>
    val currentGood: MutableLiveData<GoodCreate>
    val currentBasket: MutableLiveData<Basket>
}