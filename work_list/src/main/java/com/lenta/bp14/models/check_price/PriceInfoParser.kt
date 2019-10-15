package com.lenta.bp14.models.check_price

import javax.inject.Inject

class PriceInfoParser @Inject constructor() : IPriceInfoParser {


    override fun getPriceInfoFromRawCode(rawCode: String?): IScanPriceInfo? {

        if (rawCode == null) {
            return null
        }

        return ScanPriceInfo(
                eanCode = getEanCode(rawCode)
                        ?: return null,
                price = getPrice(rawCode)
                        ?: return null,
                discountCardPrice = getPriceWithDiscountCard(rawCode)
                        ?: return null
        ).apply {
            //Logg.d { "priceInfo: $this" }
        }

    }

    private fun getEanCode(rawCode: String): String? {
        return rawCode.substringAfter("(01)", "")
                .apply {
                    if (this.isBlank()) {
                        return null
                    }
                }
                .substringBefore("(")
    }

    private fun getPrice(rawCode: String): Double? {
        return rawCode.substringAfter("(390y)", "")
                .substringBefore("(")
                .apply {
                    if (this.isBlank()) {
                        return null
                    }
                }
                .replace(",", ".")
                .toDoubleOrNull()
    }

    private fun getPriceWithDiscountCard(rawCode: String): Double? {
        return rawCode.substringAfter("(392y)", "")
                .substringBefore("(")
                .apply {
                    if (this.isBlank()) {
                        return null
                    }
                }
                .replace(",", ".")
                .toDoubleOrNull()
    }


}

interface IPriceInfoParser {
    fun getPriceInfoFromRawCode(rawCode: String?): IScanPriceInfo?
}