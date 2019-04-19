package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// Модель справочника товаров
public class MB_S_30 {

    /// Номер товара
    @JsonProperty("MATERIAL")
    @Getter private String material;

    /// Длинное наименование
    @JsonProperty("NAME")
    @Getter private String name;

    /// Вид товара
    @JsonProperty("MATYPE")
    @Getter private String matype;

    /// Базисная единица измерения
    @JsonProperty("BUOM")
    @Getter private String buom;

    @JsonProperty("MATR_TYPE")
    @Getter private String matrType;

    /// Номер секции товара
    @JsonProperty("ABTNR")
    @Getter private String abtnr;

    /// Признак возвратности товара
    @JsonProperty("IS_RETURN")
    @Getter private String isReturn;

    /// Флаг "Акцизный товар"
    @JsonProperty("IS_EXC")
    @Getter private String exc;

    /// Флаг "Алкоголь"
    @JsonProperty("IS_ALCO")
    @Getter private String alco;

    @JsonProperty("MATKL")
    @Getter private String matkl;

    @JsonProperty("EKGRP")
    @Getter private String ekgrp;

    public MB_S_30(String material, String name, String matype, String buom, String matrType, String abtnr, String isReturn, String exc, String alco, String matkl, String ekgrp) {
        this.material = material;
        this.name = name;
        this.matype = matype;
        this.buom = buom;
        this.matrType = matrType;
        this.abtnr = abtnr;
        this.isReturn = isReturn;
        this.exc = exc;
        this.alco = alco;
        this.matkl = matkl;
        this.ekgrp = ekgrp;
    }

    public boolean isAlco()
    {
        return alco.equals("X");
    }

    public boolean IsExcise()
    {
        return exc.equals("X");
    }
}
