package com.lenta.shared.utilities.gs1

import java.io.InvalidObjectException
import java.util.*
import kotlin.math.min

object EAN128Parser {

    private const val FORMAT_AI = "%s [%s]"
    private const val EAN_WEIGHT_PREFIX = "0"

    const val EAN_01 = "01"
    const val EAN_02 = "02"

    enum class DataType {
        Numeric, Alphanumeric
    }

    class AII(var AI: String, var Description: String, var LengthOfAI: Int, var DataDescription: DataType, var LengthOfData: Int,
              var FNC1: Boolean) {
        override fun toString(): String {
            return String.format(FORMAT_AI, AI, Description)
        }
    }

    private val aiiDict: MutableMap<String, AII> = HashMap()
    private var minLengthOfAI = 1
    private var maxLengthOfAI = 4
    var groutSeperator = 29.toChar()
    var eAN128StartCode = "]C1"
    var HasCheckSum = false

    private var position = 0

    private fun addAi(ai: String, description: String, lengthOfAI: Int, dataDescription: DataType, lengthOfData: Int, fnc1: Boolean) {
        aiiDict[ai] = AII(ai, description, lengthOfAI, dataDescription, lengthOfData, fnc1)
    }

    fun parseBy(barcode: String): Map<AII, String> {
        val splitted = barcode.split("[/((?!^)\\{*?\\})/]".toRegex())
        return addToMap(splitted)
    }

    private fun addToMap(list: List<String>): Map<AII, String> {
        val result: MutableMap<AII, String> = HashMap()
        list.forEachIndexed { index, code ->
            val coded = "${code}d"
            val ai: AII? = aiiDict[code] ?: aiiDict[coded]
            ai?.let {
                val nextCodeIndex = index + 1
                if (list.size > nextCodeIndex) {
                    result[ai] = list[nextCodeIndex]
                }
            }
        }
        return result
    }

    /**
     * Main Parsing barcode function
     *
     * @param barcode [String] - scanned barcode
     * @param ai [String] - constant for GS1 identificator can be 01, 02
     * @param throwException [Boolean] - need to throw exception if code doesn't find
     *
     * @return [String] - optional value of finded code
     */
    @Throws(InvalidObjectException::class)
    fun parseWith(barcode: String, ai: String = EAN_01, throwException: Boolean = false): String? {
        var parsedBarcode: String? = null
        val allCodesMap = parse(barcode, throwException)
        val ean128Barcode = allCodesMap.entries.find { pair ->
            pair.key.AI == ai
        }?.value

        if (ean128Barcode != null) {
            parsedBarcode = if (ean128Barcode.first().toString() == EAN_WEIGHT_PREFIX) {
                ean128Barcode.substring(1 until ean128Barcode.length)
            } else {
                ean128Barcode
            }
        }
        return parsedBarcode
    }

    /**
     * Parsing function to get all entities for barcode
     * @param barcode [String] - scanned barcode
     *
     * @return [HashMap] - hash map with AII and codes
     */
    @Throws(InvalidObjectException::class)
    fun parse(barcode: String, throwException: Boolean): Map<AII, String> {
        position = 0
        var localData = barcode.replace("(", "").replace(")", "")
        // cut off the EAN128 start code
        if (localData.startsWith(eAN128StartCode)) {
            localData = localData.substring(eAN128StartCode.length)
        }
        // cut off the check sum
        if (HasCheckSum) {
            localData = localData.substring(0, localData.length - 2)
        }
        val result: MutableMap<AII, String> = HashMap()
        // walk through the EAN128 code
        while (position < localData.length) {
            // try to get the AI at the current position
            val ai: AII? = getAI(localData, position, false)
            if (ai == null) {
                if (throwException) {
                    throw InvalidObjectException("AI not found")
                }
                return result
            }
            // get the data to the current AI
            val code = getCode(localData, ai, position)
            result[ai] = code
        }
        return result
    }

    private fun getCode(data: String, ai: AII, index: Int): String {
        // get the max lenght to read.
        var lenghtToRead = min(ai.LengthOfData, data.length - index)
        // final position
        val finalReadIndex = lenghtToRead + index
        // get the data of the current AI
        var result = data.substring(index, finalReadIndex)
        // check if the AI support a group seperator
        if (ai.FNC1) {
            // try to find the index of the group seperator
            val indexOfGroupTermination = result.indexOf(groutSeperator)
            if (indexOfGroupTermination >= 0) {
                result = data.substring(index, indexOfGroupTermination + index)
                lenghtToRead = indexOfGroupTermination + 1
            } else {
                // get the data of the current AI till the group seperator with out it
                result = data.substring(index, finalReadIndex)
            }
        }

        if (finalReadIndex < data.length
                && data[finalReadIndex] == groutSeperator) {
            position++
        }

        // Shift the index to the next
        position += lenghtToRead
        return result
    }

    private fun getAI(data: String, index: Int, usePlaceHolder: Boolean): AII? {
        var result: AII? = null
        // Step through the different lenghts of the AIs
        for (i in minLengthOfAI..maxLengthOfAI + 1) {
            val addedIndex = index + i
            if (addedIndex > data.length) {
                return null
            }
            // get the AI sub string
            var ai = data.substring(index, addedIndex).replace(groutSeperator.toString(), "")
            if (usePlaceHolder && ai.isNotEmpty()) {
                ai = ai.removeRange(ai.length - 1, ai.length) + "d"
            }

            // try to get the ai from the dictionary
            result = aiiDict[ai]
            result?.let {
                position += i
                return result
            }
            // if no AI found, try it with the next lenght
        }
        // if no AI found here, than try it with placeholders. Assumed that is the first sep where usePlaceHolder is false
        if (!usePlaceHolder) {
            return getAI(data, index, true)
        }
        return result
    }

    init {
        addAi("00", "SerialShippingContainerCode", 2, DataType.Numeric, 18, false)
        addAi("01", "EAN-NumberOfTradingUnit", 2, DataType.Numeric, 14, false)
        addAi("02", "EAN-NumberOfTheWaresInTheShippingUnit", 2, DataType.Numeric, 14, false)
        addAi("10", "Charge_Number", 2, DataType.Alphanumeric, 20, true)
        addAi("11", "ProducerDate_JJMMDD", 2, DataType.Numeric, 6, false)
        addAi("12", "DueDate_JJMMDD", 2, DataType.Numeric, 6, false)
        addAi("13", "PackingDate_JJMMDD", 2, DataType.Numeric, 6, false)
        addAi("15", "MinimumDurabilityDate_JJMMDD", 2, DataType.Numeric, 6, false)
        addAi("17", "ExpiryDate_JJMMDD", 2, DataType.Numeric, 6, false)
        addAi("20", "ProductModel", 2, DataType.Numeric, 2, false)
        addAi("21", "SerialNumber", 2, DataType.Alphanumeric, 20, true)
        addAi("22", "HIBCCNumber", 2, DataType.Alphanumeric, 29, false)
        addAi("240", "PruductIdentificationOfProducer", 3, DataType.Alphanumeric, 30, true)
        addAi("241", "CustomerPartsNumber", 3, DataType.Alphanumeric, 30, true)
        addAi("250", "SerialNumberOfAIntegratedModule", 3, DataType.Alphanumeric, 30, true)
        addAi("251", "ReferenceToTheBasisUnit", 3, DataType.Alphanumeric, 30, true)
        addAi("252", "GlobalIdentifierSerialisedForTrade", 3, DataType.Numeric, 2, false)
        addAi("30", "AmountInParts", 2, DataType.Numeric, 8, true)
        addAi("310", "NetWeight_Kilogram", 2, DataType.Numeric, 8, false)
        addAi("310d", "NetWeight_Kilogram", 4, DataType.Numeric, 6, false)
        addAi("311d", "Length_Meter", 4, DataType.Numeric, 6, false)
        addAi("312d", "Width_Meter", 4, DataType.Numeric, 6, false)
        addAi("313d", "Heigth_Meter", 4, DataType.Numeric, 6, false)
        addAi("314d", "Surface_SquareMeter", 4, DataType.Numeric, 6, false)
        addAi("315d", "NetVolume_Liters", 4, DataType.Numeric, 6, false)
        addAi("316d", "NetVolume_CubicMeters", 4, DataType.Numeric, 6, false)
        addAi("320d", "NetWeight_Pounds", 4, DataType.Numeric, 6, false)
        addAi("321d", "Length_Inches", 4, DataType.Numeric, 6, false)
        addAi("322d", "Length_Feet", 4, DataType.Numeric, 6, false)
        addAi("323d", "Length_Yards", 4, DataType.Numeric, 6, false)
        addAi("324d", "Width_Inches", 4, DataType.Numeric, 6, false)
        addAi("325d", "Width_Feed", 4, DataType.Numeric, 6, false)
        addAi("326d", "Width_Yards", 4, DataType.Numeric, 6, false)
        addAi("327d", "Heigth_Inches", 4, DataType.Numeric, 6, false)
        addAi("328d", "Heigth_Feed", 4, DataType.Numeric, 6, false)
        addAi("329d", "Heigth_Yards", 4, DataType.Numeric, 6, false)
        addAi("330d", "GrossWeight_Kilogram", 4, DataType.Numeric, 6, false)
        addAi("331d", "Length_Meter", 4, DataType.Numeric, 6, false)
        addAi("332d", "Width_Meter", 4, DataType.Numeric, 6, false)
        addAi("333d", "Heigth_Meter", 4, DataType.Numeric, 6, false)
        addAi("334d", "Surface_SquareMeter", 4, DataType.Numeric, 6, false)
        addAi("335d", "GrossVolume_Liters", 4, DataType.Numeric, 6, false)
        addAi("336d", "GrossVolume_CubicMeters", 4, DataType.Numeric, 6, false)
        addAi("337d", "KilogramPerSquareMeter", 4, DataType.Numeric, 6, false)
        addAi("340d", "GrossWeight_Pounds", 4, DataType.Numeric, 6, false)
        addAi("341d", "Length_Inches", 4, DataType.Numeric, 6, false)
        addAi("342d", "Length_Feet", 4, DataType.Numeric, 6, false)
        addAi("343d", "Length_Yards", 4, DataType.Numeric, 6, false)
        addAi("344d", "Width_Inches", 4, DataType.Numeric, 6, false)
        addAi("345d", "Width_Feed", 4, DataType.Numeric, 6, false)
        addAi("346d", "Width_Yards", 4, DataType.Numeric, 6, false)
        addAi("347d", "Heigth_Inches", 4, DataType.Numeric, 6, false)
        addAi("348d", "Heigth_Feed", 4, DataType.Numeric, 6, false)
        addAi("349d", "Heigth_Yards", 4, DataType.Numeric, 6, false)
        addAi("350d", "Surface_SquareInches", 4, DataType.Numeric, 6, false)
        addAi("351d", "Surface_SquareFeet", 4, DataType.Numeric, 6, false)
        addAi("352d", "Surface_SquareYards", 4, DataType.Numeric, 6, false)
        addAi("353d", "Surface_SquareInches", 4, DataType.Numeric, 6, false)
        addAi("354d", "Surface_SquareFeed", 4, DataType.Numeric, 6, false)
        addAi("355d", "Surface_SquareYards", 4, DataType.Numeric, 6, false)
        addAi("356d", "NetWeight_TroyOunces", 4, DataType.Numeric, 6, false)
        addAi("357d", "NetVolume_Ounces", 4, DataType.Numeric, 6, false)
        addAi("360d", "NetVolume_Quarts", 4, DataType.Numeric, 6, false)
        addAi("361d", "NetVolume_Gallonen", 4, DataType.Numeric, 6, false)
        addAi("362d", "GrossVolume_Quarts", 4, DataType.Numeric, 6, false)
        addAi("363d", "GrossVolume_Gallonen", 4, DataType.Numeric, 6, false)
        addAi("364d", "NetVolume_CubicInches", 4, DataType.Numeric, 6, false)
        addAi("365d", "NetVolume_CubicFeet", 4, DataType.Numeric, 6, false)
        addAi("366d", "NetVolume_CubicYards", 4, DataType.Numeric, 6, false)
        addAi("367d", "GrossVolume_CubicInches", 4, DataType.Numeric, 6, false)
        addAi("368d", "GrossVolume_CubicFeet", 4, DataType.Numeric, 6, false)
        addAi("369d", "GrossVolume_CubicYards", 4, DataType.Numeric, 6, false)
        addAi("37", "QuantityInParts", 2, DataType.Numeric, 8, true)
        addAi("390d", "AmountDue_DefinedValutaBand", 4, DataType.Numeric, 15, true)
        addAi("391d", "AmountDue_WithISOValutaCode", 4, DataType.Numeric, 18, true)
        addAi("392d", "BePayingAmount_DefinedValutaBand", 4, DataType.Numeric, 15, true)
        addAi("393d", "BePayingAmount_WithISOValutaCode", 4, DataType.Numeric, 18, true)
        addAi("400", "JobNumberOfGoodsRecipient", 3, DataType.Alphanumeric, 30, true)
        addAi("401", "ShippingNumber", 3, DataType.Alphanumeric, 30, true)
        addAi("402", "DeliveryNumber", 3, DataType.Numeric, 17, false)
        addAi("403", "RoutingCode", 3, DataType.Alphanumeric, 30, true)
        addAi("410", "EAN_UCC_GlobalLocationNumber(GLN)_GoodsRecipient", 3, DataType.Numeric, 13, false)
        addAi("411", "EAN_UCC_GlobalLocationNumber(GLN)_InvoiceRecipient", 3, DataType.Numeric, 13, false)
        addAi("412", "EAN_UCC_GlobalLocationNumber(GLN)_Distributor", 3, DataType.Numeric, 13, false)
        addAi("413", "EAN_UCC_GlobalLocationNumber(GLN)_FinalRecipient", 3, DataType.Numeric, 13, false)
        addAi("414", "EAN_UCC_GlobalLocationNumber(GLN)_PhysicalLocation", 3, DataType.Numeric, 13, false)
        addAi("415", "EAN_UCC_GlobalLocationNumber(GLN)_ToBilligParticipant", 3, DataType.Numeric, 13, false)
        addAi("420", "ZipCodeOfRecipient_withoutCountryCode", 3, DataType.Alphanumeric, 20, true)
        addAi("421", "ZipCodeOfRecipient_withCountryCode", 3, DataType.Alphanumeric, 12, true)
        addAi("422", "BasisCountryOfTheWares_ISO3166Format", 3, DataType.Numeric, 3, false)
        addAi("7001", "Nato Stock Number", 4, DataType.Numeric, 13, false)
        addAi("7003", "DataAndTimeOfManufacturing", 4, DataType.Alphanumeric, 10, true)
        addAi("8001", "RolesProducts", 4, DataType.Numeric, 14, false)
        addAi("8002", "SerialNumberForMobilePhones", 4, DataType.Alphanumeric, 20, true)
        addAi("8003", "GlobalReturnableAssetIdentifier", 4, DataType.Alphanumeric, 34, true)
        addAi("8004", "GlobalIndividualAssetIdentifier", 4, DataType.Numeric, 30, true)
        addAi("8005", "SalesPricePerUnit", 4, DataType.Numeric, 6, false)
        addAi("8006", "IdentifikationOfAProductComponent", 4, DataType.Numeric, 18, false)
        addAi("8007", "IBAN", 4, DataType.Alphanumeric, 30, true)
        addAi("8008", "DataAndTimeOfManufacturing", 4, DataType.Numeric, 12, true)
        addAi("8018", "GlobalServiceRelationNumber", 4, DataType.Numeric, 18, false)
        addAi("8020", "NumberBillCoverNumber", 4, DataType.Alphanumeric, 25, false)
        addAi("8100", "CouponExtendedCode_NSC_offerCcode", 4, DataType.Numeric, 10, false)
        addAi("8101", "CouponExtendedCode_NSC_offerCcode_EndOfOfferCode", 4, DataType.Numeric, 14, false)
        addAi("8102", "CouponExtendedCode_NSC", 4, DataType.Numeric, 6, false)
        addAi("90", "InformationForBilateralCoordinatedApplications", 2, DataType.Alphanumeric, 30, true)
        addAi("91", "Company specific", 2, DataType.Alphanumeric, 30, true)
        addAi("92", "Company specific", 2, DataType.Alphanumeric, 30, true)
        //Add("93", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("94", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("95", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("96", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("97", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("98", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("99", "Company specific", 2, DataType.Alphanumeric, 30, true);
        minLengthOfAI = aiiDict.values.minBy { it.LengthOfAI }?.LengthOfAI ?: 0
        maxLengthOfAI = aiiDict.values.maxBy { it.LengthOfAI }?.LengthOfAI ?: 2
    }
}