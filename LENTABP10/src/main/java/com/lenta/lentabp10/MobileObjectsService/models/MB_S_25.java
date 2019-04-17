package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// модель справочника штрих-кодов единиц измерения товаров
public class MB_S_25 {
    /// Глобальный номер товара (GTIN)
    @JsonProperty("EAN")
    @Getter private String ean;

    /// Номер товара
    @JsonProperty("MATERIAL")
    @Getter private String material;

    /// Единица измерения
    @JsonProperty("UOM")
    @Getter private String uom;

    @JsonProperty("UMREZ")
    @Getter private double umrez;

    @JsonProperty("UMREN")
    @Getter private double umren;

    public MB_S_25(String ean, String material, String uom, double umrez, double umren) {
        this.ean = ean;
        this.material = material;
        this.uom = uom;
        this.umrez = umrez;
        this.umren = umren;
    }

    public double getUomMultiplier()
    {
        return umrez / umren;
    }
}
