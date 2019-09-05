package com.lenta.bp14.models.check_price

class PriceInfoParser : IPriceInfoParser {


    override fun getPriceInfoFromRawCode(rawCode: String?): IPriceInfo? {

        if (rawCode == null) {
            return null
        }

        return PriceInfo(
                eanCode = getEanCode(rawCode)
                        ?: return null,
                matNr = null,
                price = getPrice(rawCode)
                        ?: return null,
                discountCardPrice = getPriceWithDiscountCard(rawCode)
                        ?: return null
        )

    }

    private fun getEanCode(rawCode: String): String? {
        return rawCode.substringAfter("(01)").substringBefore("(")
    }

    private fun getPrice(rawCode: String): Float? {
        return rawCode.substringAfter("(390y)")
                .substringBefore("(")
                .replace(",", ".")
                .toFloatOrNull()
    }

    private fun getPriceWithDiscountCard(rawCode: String): Float? {
        return rawCode.substringAfter("(392y)")
                .substringBefore("(")
                .replace(",", ".")
                .toFloatOrNull()
    }


}

interface IPriceInfoParser {
    fun getPriceInfoFromRawCode(rawCode: String?): IPriceInfo?
}