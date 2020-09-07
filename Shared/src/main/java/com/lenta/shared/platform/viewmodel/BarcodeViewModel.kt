package com.lenta.shared.platform.viewmodel

import com.lenta.shared.models.core.BarcodeData
import com.lenta.shared.models.core.BarcodeInfo
import com.lenta.shared.models.core.Batch
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.gs1.*

abstract class BarcodeViewModel : CoreViewModel() {

    protected open val weightValue by unsafeLazy { listOf(VALUE_23, VALUE_24, VALUE_27, VALUE_28) }
    protected open val weightZeroValue by unsafeLazy { listOf(VALUE_023, VALUE_024, VALUE_027, VALUE_028) }

    protected open suspend fun processBarcode(data: String): BarcodeData {
        var barcode = data
        val barcodeLenght = barcode.length
        var isWeight = false
        var weight = DEFAULT_ZERO_VALUE
        var batch: Batch? = null

        when {
            // SAP-код (только ручной ввод)
            barcodeLenght in 0..MAX_SAP_CODE_LENGTH -> {
                if (GTIN.convertibleToGTIN8(data)) {
                    barcode = GTIN.toGTIN8(data)
                }
                isWeight = GTIN.isVariableMeasureItem(barcode)
                if (isWeight) {
                    val changedBarcode = getChangedBarcodeAndWeight(barcode)
                    barcode = changedBarcode.first
                    weight = changedBarcode.second
                }
            }
            //EAN (ручной ввод и сканирование)
            barcodeLenght in MAX_SAP_CODE_LENGTH..MAX_EAN_CODE_LENGTH -> {
                isWeight = getIsWeightFromCode(barcode)
                if (isWeight) {
                    val changedBarcode = getChangedBarcodeAndWeight(barcode)
                    barcode = changedBarcode.first
                    weight = changedBarcode.second
                }
            }
            // парсинг кодов GS1 с разделителями
            barcodeLenght >= MINIMUM_GS1_CODE_LENGTH -> {
                val parsedEntities = EAN128Parser.parse(barcode, false)
                barcode = parsedEntities.getString(ApplicationIdentifier.GTIN)
                weight = parsedEntities.getString(ApplicationIdentifier.ITEM_NET_WEIGHT_KG)
                isWeight = GTIN.isVariableMeasureItem(barcode)
                val batchNumber = parsedEntities.getString(ApplicationIdentifier.BATCH_OR_LOT_NUMBER)
                val count = parsedEntities.getDouble(ApplicationIdentifier.COUNT_OF_TRADE_ITEMS)
                val dateProduction = parsedEntities.getDate(ApplicationIdentifier.PRODUCTION_DATE_AND_TIME)
                val dateExpiration = parsedEntities.getDate(ApplicationIdentifier.EXPIRATION_DATE_AND_TIME)

                batch = Batch(batchNumber, count, dateProduction, dateExpiration)
            }
        }

        return BarcodeData(
                barcodeInfo = BarcodeInfo(
                        barcode = barcode,
                        isWeight = isWeight,
                        weight = weight.toDouble()

                ), batch = batch
        )
    }

    protected open fun getChangedBarcodeAndWeight(barcode: String): Pair<String, String> {
        val changedBarcode = barcode.replace(barcode.takeLast(6), TAKEN_ZEROS)
        val weight = barcode.takeLast(6).take(5)
        return changedBarcode to weight
    }

    private fun getIsWeightFromCode(barcode: String): Boolean {
        return if (barcode.length < MAX_EAN_CODE_LENGTH) {
            val collidedBarcode = DEFAULT_ZERO_VALUE + barcode
            val weightCollider = collidedBarcode.substring(0 until 3)
            weightZeroValue.any { weightCollider == it }
        } else {
            val weightCollider = barcode.substring(0 until 2)
            weightValue.any { weightCollider == it }
        }
    }

    companion object {
        private const val VALUE_23 = "23"
        private const val VALUE_023 = "023"
        private const val VALUE_24 = "24"
        private const val VALUE_024 = "024"
        private const val VALUE_27 = "27"
        private const val VALUE_027 = "027"
        private const val VALUE_28 = "28"
        private const val VALUE_028 = "028"

        private const val MAX_SAP_CODE_LENGTH = 8
        private const val MAX_EAN_CODE_LENGTH = 14
        private const val MINIMUM_GS1_CODE_LENGTH = 16
        private const val TAKEN_ZEROS = "000000"
        private const val DEFAULT_ZERO_VALUE = "0"
    }
}