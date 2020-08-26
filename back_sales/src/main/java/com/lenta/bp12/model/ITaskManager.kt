package com.lenta.bp12.model

import com.lenta.bp12.model.pojo.Good

interface ITaskManager {
    fun isGoodInTask(good: Good)
}