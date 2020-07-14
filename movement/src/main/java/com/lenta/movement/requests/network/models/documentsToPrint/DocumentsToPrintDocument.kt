package com.lenta.movement.requests.network.models.documentsToPrint

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class DocumentsToPrintDocument(
        @SerializedName("DOC_ID")
        val docId: String?,

        @SerializedName("DOC_NAME")
        val docName: String?
) : Parcelable