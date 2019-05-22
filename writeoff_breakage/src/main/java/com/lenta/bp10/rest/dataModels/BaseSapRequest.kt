package com.lenta.bp10.rest.dataModels

//import com.fasterxml.jackson.annotation.SerializedName
import com.google.gson.annotations.SerializedName

open class BaseSapRequest(
        @SerializedName("format")
        val format: String,
        @SerializedName("sap-client")
        var sapClient: String?)