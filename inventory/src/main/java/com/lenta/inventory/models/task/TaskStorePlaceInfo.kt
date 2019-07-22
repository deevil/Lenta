package com.lenta.inventory.models.task

import com.lenta.inventory.models.StorePlaceStatus

class TaskStorePlaceInfo(val placeCode: String,	//Код места хранения
                         val status: StorePlaceStatus,
                         val lockUser: String,	//Имя пользователя
                         val lockIP: String,
                         var isProcessed: Boolean = false
                        ) {
}
