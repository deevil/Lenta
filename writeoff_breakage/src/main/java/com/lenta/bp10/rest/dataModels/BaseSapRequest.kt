package com.lenta.bp10.rest.dataModels

import com.fasterxml.jackson.annotation.JsonProperty

open class BaseSapRequest(
        @JsonProperty("format")
        val format: String,
        @JsonProperty("sap-client")
        var sapClient: String?)