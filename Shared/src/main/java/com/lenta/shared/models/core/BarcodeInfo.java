package com.lenta.shared.models.core;

import lombok.Getter;

public class BarcodeInfo {
    @Getter private boolean isWeight;
    @Getter private double weight;
    @Getter private String barcode;
    @Getter private String barcodeUom;
    @Getter private String productMaterial;

    public BarcodeInfo(String barcode) {
        this (barcode, null, null, false, 0);
    }

    public BarcodeInfo(String barcode, boolean isWeight, double weight) {
        this(barcode, null, null, isWeight, weight);
    }

    public BarcodeInfo(String barcode, String productMaterial, String barcodeUom, boolean isWeight, double weight)
    {
        this.barcode = barcode;
        this.productMaterial = productMaterial;
        this.barcodeUom = barcodeUom;
        this.isWeight = isWeight;
        this.weight = weight;
    }
}
