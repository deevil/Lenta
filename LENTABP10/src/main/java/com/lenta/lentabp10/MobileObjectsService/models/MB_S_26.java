package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// модель справочника принтеров
public class MB_S_26 {

    /// Номер ТК
    @JsonProperty("WERKS")
    @Getter private String tkNumber;

    /// Наименование принтера
    @JsonProperty("PRINTERNAME")
    @Getter private String printerName;

    /// Информация о принтере
    @JsonProperty("PRINTERINFO")
    @Getter private String printerInfo;

    public MB_S_26(String tkNumber, String printerName, String printerInfo) {
        this.tkNumber = tkNumber;
        this.printerName = printerName;
        this.printerInfo = printerInfo;
    }
}
