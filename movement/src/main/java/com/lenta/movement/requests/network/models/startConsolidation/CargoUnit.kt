package com.lenta.movement.requests.network.models.startConsolidation

import com.google.gson.annotations.SerializedName

/**"ET_TASK_EXIDV_TOP" Список ГЕ*/
data class CargoUnit(
        /** Номер ГЕ */
        @SerializedName("EXIDV_TOP")
        val cargoUnitNumber: String,

        /** Номер ЕО */
        @SerializedName("EXIDV")
        val processingUnitNumber: String
)