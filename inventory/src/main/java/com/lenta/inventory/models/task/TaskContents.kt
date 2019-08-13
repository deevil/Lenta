package com.lenta.inventory.models.task

import com.lenta.shared.models.core.ExciseStamp

class TaskContents (val deadline: String,
                    val linkOldStamp: Boolean,
                    val products: List<TaskProductInfo>,
                    val storePlaces: List<TaskStorePlaceInfo>,
                    val exciseStamps: List<TaskExciseStamp>) {

}