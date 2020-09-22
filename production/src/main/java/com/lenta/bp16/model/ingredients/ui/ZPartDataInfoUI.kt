package com.lenta.bp16.model.ingredients.ui

import com.lenta.bp16.model.IDataInfo
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_dd_mm_yyyy
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_yyyy_mm_dd
import com.lenta.shared.utilities.getFormattedDate

data class ZPartDataInfoUI (
        /** SAP-код */
        val matnr: String,
        /** Номер партии */
        val batchId: String,
        /** Код производителя */
        override val prodCode: String,
        /** Наименование производителя */
        override val prodName: String,
        /** Дата производства */
        override val prodDate: String
): IDataInfo {
        fun formattedDate(): String{
                return getFormattedDate(prodDate, DATE_FORMAT_yyyy_mm_dd, DATE_FORMAT_dd_mm_yyyy)
        }
}