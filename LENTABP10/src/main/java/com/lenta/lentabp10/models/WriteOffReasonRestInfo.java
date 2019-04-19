package com.lenta.lentabp10.models;

/// <summary>
/// Мат номер, элемент списка IT_MATERIALS в SaveTaskDataToSapRestRequest
/// </summary>

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class WriteOffReasonRestInfo {

    /// Номер товара
    @JsonProperty("MATNR")
    @Getter private String matnr;

    /// Причина движения
    @JsonProperty("GRUND")
    @Getter private String writeOffCause;

    /// Место возникновения затрат
    @JsonProperty("KOSTL")
    @Getter private String kostl;

    /// Введенное количество
    @JsonProperty("FIRST_QNT")
    @Getter private String amount;

    public WriteOffReasonRestInfo(String matnr, String writeOffCause, String kostl, String amount) {
        this.matnr = matnr;
        this.writeOffCause = writeOffCause;
        this.kostl = kostl;
        this.amount = amount;
    }
}
