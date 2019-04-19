package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/// модель справочник единиц измерения
/// ZMP_UTZ_07_V001
public class MB_S_07 {
    /// Единица измерения
    @JsonProperty("UOM")
    @Getter private String uom;

    /// Внешняя ЕИ - коммерческое представление (3-значная)
    @JsonProperty("NAME")
    @Getter private String name;

    /// Число десятичных разрядов при представлении чисел
    @JsonProperty("DECAN")
    @Getter private short decan;

    public MB_S_07(String uom, String name, short decan) {
        this.uom = uom;
        this.name = name;
        this.decan = decan;
    }
}
