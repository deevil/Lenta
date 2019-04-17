package com.lenta.lentabp10.rest.DataModels;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class BaseSapRequest {

    @JsonProperty("format")
    @Getter private String format;

    @JsonProperty("sap-client")
    @Getter private String sapClient;

    public BaseSapRequest(String format, String sapClient) {
        this.format = format;
        this.sapClient = sapClient;
    }
}
