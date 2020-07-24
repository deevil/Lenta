package com.lenta.movement.requests.network.models.startConsolidation

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**"ET_TASK_POS"*/

@Parcelize
data class StartConsolidationTaskComposition(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val materialNumber: String?,

        /** Номер ЕО */
        @SerializedName("EXIDV")
        val processingUnitNumber: String?,

        /** Единица измерения заказа на поставку */
        @SerializedName("BSTME")
        val orderUnits: String?,

        /** Объем заказа */
        @SerializedName("MENGE")
        val quantity: String?
) : Parcelable