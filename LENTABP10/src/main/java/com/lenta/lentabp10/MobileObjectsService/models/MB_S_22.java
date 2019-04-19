package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// Модель справочника списка наборов
public class MB_S_22 {
    /// Номер товара
    @JsonProperty("MATNR_OSN")
    @Getter private String matnr_osn;

    /// Компонент спецификации
    @JsonProperty("MATNR")
    @Getter private String matnr;

    /// Количество вложенного
    @JsonProperty("MENGE")
    @Getter private double menge;

    /// Базисная единица измерения
    @JsonProperty("MEINS")
    @Getter private String meins;

    public MB_S_22(String matnr_osn, String matnr, double menge, String meins) {
        this.matnr_osn = matnr_osn;
        this.matnr = matnr;
        this.menge = menge;
        this.meins = meins;
    }
}
