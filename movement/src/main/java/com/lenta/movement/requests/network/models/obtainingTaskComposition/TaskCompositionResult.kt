package com.lenta.movement.requests.network.models.obtainingTaskComposition

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.models.core.Supplier

data class TaskCompositionResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val taskComposition: List<TaskComposition>,

        /** Таблица корзин задания*/
        @SerializedName("ET_TASK_BASKET")
        val basketList: List<TaskBasket>,

        @SerializedName("ET_TASK_MARK")
        val stampsList: List<TaskExciseStampInfoRestData>,

        @SerializedName("ET_TASK_PARTS")
        val taskBatches: List<TaskBatchInfoRestData>,

        @SerializedName("ET_MATERIALS")
        val fullProductsInfoList: List<TaskFullProductInfo>,

        @SerializedName("ET_PROD")
        val manufacturers: List<Manufacturer>,

        @SerializedName("ET_LIFNR")
        val suppliers: List<Supplier>
)