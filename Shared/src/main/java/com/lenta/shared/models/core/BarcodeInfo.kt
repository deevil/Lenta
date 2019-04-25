package com.lenta.shared.models.core

class BarcodeInfo (val barcode: String, val productMaterial: String?, val barcodeUom: String?, val isWeight: Boolean, val weight: Double) {

    constructor(barcode: String) : this(barcode, null, null, false, 0.0)

    constructor(barcode: String, isWeight: Boolean, weight: Double) : this(barcode, null, null, isWeight, weight)

}