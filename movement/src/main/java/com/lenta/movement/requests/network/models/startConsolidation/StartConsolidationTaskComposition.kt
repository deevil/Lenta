package com.lenta.movement.requests.network.models.startConsolidation

import com.google.gson.annotations.SerializedName

/**"ET_TASK_POS"*/
data class StartConsolidationTaskComposition(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val materialNumber: String,

        /** Номер ЕО */
        @SerializedName("EXIDV")
        val processingUnitNumber: String,

        /** Единица измерения заказа на поставку */
        @SerializedName("BSTME")
        val orderUnits: String,

        /** Объем заказа */
        @SerializedName("MENGE")
        val quantity: String
)