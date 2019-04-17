package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// модель значений параметров настройки
public class MB_S_14 {

    /// Название параметра
    @JsonProperty("PARAMNAME")
    @Getter private String paramname;

    /// Значение параметра
    @JsonProperty("PARAMVALUE")
    @Getter private String paramvalue;

    public MB_S_14(String paramname, String paramvalue) {
        this.paramname = paramname;
        this.paramvalue = paramvalue;
    }
}
