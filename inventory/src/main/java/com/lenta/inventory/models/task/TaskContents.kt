package com.lenta.inventory.models.task


data class TaskContents(val deadline: String,
                        val linkOldStamp: Boolean,
                        val products: List<TaskProductInfo>,
                        val storePlaces: List<TaskStorePlaceInfo>,
                        val exciseStamps: List<TaskExciseStamp>,
                        val minUpdSales: Long?)