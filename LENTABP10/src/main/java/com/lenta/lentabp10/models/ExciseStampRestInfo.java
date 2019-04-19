package com.lenta.lentabp10.models;

/// <summary>
/// Акцизная марка, элемент списка IT_MARKS в SaveTaskDataToSapRestRequest
/// </summary>

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class ExciseStampRestInfo {
    /// Номер набора (null для ненабора)
    @JsonProperty("MATNR_OSN")
    @Getter private String matnrOsn;

    /// Номер товара
    @JsonProperty("MATNR")
    @Getter private String matnr;

    /// Причина движения
    @JsonProperty("GRUND")
    @Getter private String writeOffCause;

    /// Код акцизной марки
    @JsonProperty("PDF417")
    @Getter private String stamp;

    /// Признак bad mark
    @JsonProperty("REG")
    @Getter private String reg;

    public ExciseStampRestInfo(String matnrOsn, String matnr, String writeOffCause, String stamp, String reg) {
        this.matnrOsn = matnrOsn;
        this.matnr = matnr;
        this.writeOffCause = writeOffCause;
        this.stamp = stamp;
        this.reg = reg;
    }
}
