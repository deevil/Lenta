package com.lenta.shared.models.core

class BarcodeInfo(val barcode: String, val productMaterial: String?, val barcodeUom: String?, val isWeight: Boolean, val weight: String) {

    constructor(barcode: String) : this(barcode, null, null, false, "0")

    constructor(barcode: String, isWeight: Boolean, weight: String) : this(barcode, null, null, isWeight, weight)

}