package com.lenta.shared.utilities

import java.io.InvalidObjectException
import java.util.*
import kotlin.math.min

object EAN128Parser {
    enum class DataType {
        Numeric, Alphanumeric
    }

    class AII(var AI: String, var Description: String, var LengthOfAI: Int, var DataDescription: DataType, var LengthOfData: Int,
              var FNC1: Boolean) {
        override fun toString(): String {
            return String.format("%s [%s]", AI, Description)
        }
    }

    private val aiiDict: MutableMap<String, AII> = HashMap()
    private var minLengthOfAI = 1
    private var maxLengthOfAI = 4
    var groutSeperator = 29.toChar()
    var eAN128StartCode = "]C1"
    var HasCheckSum = false

    fun Add(ai: String, description: String, lengthOfAI: Int, dataDescription: DataType, lengthOfData: Int, fnc1: Boolean) {
        aiiDict[ai] = AII(ai, description, lengthOfAI, dataDescription, lengthOfData, fnc1)
    }

    @Throws(InvalidObjectException::class)
    fun parse(data: String, throwException: Boolean): Map<AII, String> {
        var localData = data
        // cut off the EAN128 start code
        if (localData.startsWith(eAN128StartCode)) {
            localData = localData.substring(eAN128StartCode.length)
        }
        // cut off the check sum
        if (HasCheckSum) {
            localData = localData.substring(0, localData.length - 2)
        }
        val result: MutableMap<AII, String> = HashMap()
        var index = 0
        // walk through the EAN128 code
        while (index < localData.length) {
            // try to get the AI at the current position
            val (ai: AII?, count: Int) = getAI(localData, index, false)
            if (ai == null) {
                if (throwException) {
                    throw InvalidObjectException("AI not found")
                }
                return result
            } else {
                // Shift the index to the next
                index += count
            }
            // get the data to the current AI
            val code = getCode(localData, ai, index)
            result[ai] = code
        }
        return result
    }

    private fun getCode(data: String, ai: AII, index: Int): String {
        // get the max lenght to read.
        var localIndex = index
        var lenghtToRead = min(ai.LengthOfData, data.length - localIndex)
        // get the data of the current AI
        var result = data.substring(localIndex, lenghtToRead + localIndex)
        // check if the AI support a group seperator
        if (ai.FNC1) {
            // try to find the index of the group seperator
            val indexOfGroupTermination = result.indexOf(groutSeperator)
            if (indexOfGroupTermination >= 0) {
                result = data.substring(localIndex, indexOfGroupTermination + localIndex)
                lenghtToRead = indexOfGroupTermination + 1
            } else {
                // get the data of the current AI till the group seperator with out it
                result = data.substring(localIndex, lenghtToRead + localIndex)
            }
        }

        if (localIndex + lenghtToRead < data.length
                && data[localIndex + lenghtToRead] == groutSeperator) {
            lenghtToRead++
        }

        // Shift the index to the next
        localIndex += lenghtToRead
        return result
    }

    private fun getAI(data: String, index: Int, usePlaceHolder: Boolean): Pair<AII?, Int> {
        var result: AII? = null
        // Step through the different lenghts of the AIs
        for (i in minLengthOfAI..maxLengthOfAI) {
            val addedIndex = index + i
            if (addedIndex > data.length) {
                return null to i
            }
            // get the AI sub string
            var ai = data.substring(index, addedIndex)
            if (usePlaceHolder && ai.isNotEmpty()) {
                ai = ai.removeRange(ai.length - 1, ai.length) + "d"
            }

            // try to get the ai from the dictionary
            result = aiiDict[ai]
            result?.let {
                return result to i
            }
            // if no AI found, try it with the next lenght
        }
        // if no AI found here, than try it with placeholders. Assumed that is the first sep where usePlaceHolder is false
        if (!usePlaceHolder) {
            return getAI(data, index, true)
        }
        return result to 0
    }

    init {
        Add("00", "SerialShippingContainerCode", 2, DataType.Numeric, 18, false)
        Add("01", "EAN-NumberOfTradingUnit", 2, DataType.Numeric, 14, false)
        Add("02", "EAN-NumberOfTheWaresInTheShippingUnit", 2, DataType.Numeric, 14, false)
        Add("10", "Charge_Number", 2, DataType.Alphanumeric, 20, true)
        Add("11", "ProducerDate_JJMMDD", 2, DataType.Numeric, 6, false)
        Add("12", "DueDate_JJMMDD", 2, DataType.Numeric, 6, false)
        Add("13", "PackingDate_JJMMDD", 2, DataType.Numeric, 6, false)
        Add("15", "MinimumDurabilityDate_JJMMDD", 2, DataType.Numeric, 6, false)
        Add("17", "ExpiryDate_JJMMDD", 2, DataType.Numeric, 6, false)
        Add("20", "ProductModel", 2, DataType.Numeric, 2, false)
        Add("21", "SerialNumber", 2, DataType.Alphanumeric, 20, true)
        Add("22", "HIBCCNumber", 2, DataType.Alphanumeric, 29, false)
        Add("240", "PruductIdentificationOfProducer", 3, DataType.Alphanumeric, 30, true)
        Add("241", "CustomerPartsNumber", 3, DataType.Alphanumeric, 30, true)
        Add("250", "SerialNumberOfAIntegratedModule", 3, DataType.Alphanumeric, 30, true)
        Add("251", "ReferenceToTheBasisUnit", 3, DataType.Alphanumeric, 30, true)
        Add("252", "GlobalIdentifierSerialisedForTrade", 3, DataType.Numeric, 2, false)
        Add("30", "AmountInParts", 2, DataType.Numeric, 8, true)
        Add("310d", "NetWeight_Kilogram", 4, DataType.Numeric, 6, false)
        Add("311d", "Length_Meter", 4, DataType.Numeric, 6, false)
        Add("312d", "Width_Meter", 4, DataType.Numeric, 6, false)
        Add("313d", "Heigth_Meter", 4, DataType.Numeric, 6, false)
        Add("314d", "Surface_SquareMeter", 4, DataType.Numeric, 6, false)
        Add("315d", "NetVolume_Liters", 4, DataType.Numeric, 6, false)
        Add("316d", "NetVolume_CubicMeters", 4, DataType.Numeric, 6, false)
        Add("320d", "NetWeight_Pounds", 4, DataType.Numeric, 6, false)
        Add("321d", "Length_Inches", 4, DataType.Numeric, 6, false)
        Add("322d", "Length_Feet", 4, DataType.Numeric, 6, false)
        Add("323d", "Length_Yards", 4, DataType.Numeric, 6, false)
        Add("324d", "Width_Inches", 4, DataType.Numeric, 6, false)
        Add("325d", "Width_Feed", 4, DataType.Numeric, 6, false)
        Add("326d", "Width_Yards", 4, DataType.Numeric, 6, false)
        Add("327d", "Heigth_Inches", 4, DataType.Numeric, 6, false)
        Add("328d", "Heigth_Feed", 4, DataType.Numeric, 6, false)
        Add("329d", "Heigth_Yards", 4, DataType.Numeric, 6, false)
        Add("330d", "GrossWeight_Kilogram", 4, DataType.Numeric, 6, false)
        Add("331d", "Length_Meter", 4, DataType.Numeric, 6, false)
        Add("332d", "Width_Meter", 4, DataType.Numeric, 6, false)
        Add("333d", "Heigth_Meter", 4, DataType.Numeric, 6, false)
        Add("334d", "Surface_SquareMeter", 4, DataType.Numeric, 6, false)
        Add("335d", "GrossVolume_Liters", 4, DataType.Numeric, 6, false)
        Add("336d", "GrossVolume_CubicMeters", 4, DataType.Numeric, 6, false)
        Add("337d", "KilogramPerSquareMeter", 4, DataType.Numeric, 6, false)
        Add("340d", "GrossWeight_Pounds", 4, DataType.Numeric, 6, false)
        Add("341d", "Length_Inches", 4, DataType.Numeric, 6, false)
        Add("342d", "Length_Feet", 4, DataType.Numeric, 6, false)
        Add("343d", "Length_Yards", 4, DataType.Numeric, 6, false)
        Add("344d", "Width_Inches", 4, DataType.Numeric, 6, false)
        Add("345d", "Width_Feed", 4, DataType.Numeric, 6, false)
        Add("346d", "Width_Yards", 4, DataType.Numeric, 6, false)
        Add("347d", "Heigth_Inches", 4, DataType.Numeric, 6, false)
        Add("348d", "Heigth_Feed", 4, DataType.Numeric, 6, false)
        Add("349d", "Heigth_Yards", 4, DataType.Numeric, 6, false)
        Add("350d", "Surface_SquareInches", 4, DataType.Numeric, 6, false)
        Add("351d", "Surface_SquareFeet", 4, DataType.Numeric, 6, false)
        Add("352d", "Surface_SquareYards", 4, DataType.Numeric, 6, false)
        Add("353d", "Surface_SquareInches", 4, DataType.Numeric, 6, false)
        Add("354d", "Surface_SquareFeed", 4, DataType.Numeric, 6, false)
        Add("355d", "Surface_SquareYards", 4, DataType.Numeric, 6, false)
        Add("356d", "NetWeight_TroyOunces", 4, DataType.Numeric, 6, false)
        Add("357d", "NetVolume_Ounces", 4, DataType.Numeric, 6, false)
        Add("360d", "NetVolume_Quarts", 4, DataType.Numeric, 6, false)
        Add("361d", "NetVolume_Gallonen", 4, DataType.Numeric, 6, false)
        Add("362d", "GrossVolume_Quarts", 4, DataType.Numeric, 6, false)
        Add("363d", "GrossVolume_Gallonen", 4, DataType.Numeric, 6, false)
        Add("364d", "NetVolume_CubicInches", 4, DataType.Numeric, 6, false)
        Add("365d", "NetVolume_CubicFeet", 4, DataType.Numeric, 6, false)
        Add("366d", "NetVolume_CubicYards", 4, DataType.Numeric, 6, false)
        Add("367d", "GrossVolume_CubicInches", 4, DataType.Numeric, 6, false)
        Add("368d", "GrossVolume_CubicFeet", 4, DataType.Numeric, 6, false)
        Add("369d", "GrossVolume_CubicYards", 4, DataType.Numeric, 6, false)
        Add("37", "QuantityInParts", 2, DataType.Numeric, 8, true)
        Add("390d", "AmountDue_DefinedValutaBand", 4, DataType.Numeric, 15, true)
        Add("391d", "AmountDue_WithISOValutaCode", 4, DataType.Numeric, 18, true)
        Add("392d", "BePayingAmount_DefinedValutaBand", 4, DataType.Numeric, 15, true)
        Add("393d", "BePayingAmount_WithISOValutaCode", 4, DataType.Numeric, 18, true)
        Add("400", "JobNumberOfGoodsRecipient", 3, DataType.Alphanumeric, 30, true)
        Add("401", "ShippingNumber", 3, DataType.Alphanumeric, 30, true)
        Add("402", "DeliveryNumber", 3, DataType.Numeric, 17, false)
        Add("403", "RoutingCode", 3, DataType.Alphanumeric, 30, true)
        Add("410", "EAN_UCC_GlobalLocationNumber(GLN)_GoodsRecipient", 3, DataType.Numeric, 13, false)
        Add("411", "EAN_UCC_GlobalLocationNumber(GLN)_InvoiceRecipient", 3, DataType.Numeric, 13, false)
        Add("412", "EAN_UCC_GlobalLocationNumber(GLN)_Distributor", 3, DataType.Numeric, 13, false)
        Add("413", "EAN_UCC_GlobalLocationNumber(GLN)_FinalRecipient", 3, DataType.Numeric, 13, false)
        Add("414", "EAN_UCC_GlobalLocationNumber(GLN)_PhysicalLocation", 3, DataType.Numeric, 13, false)
        Add("415", "EAN_UCC_GlobalLocationNumber(GLN)_ToBilligParticipant", 3, DataType.Numeric, 13, false)
        Add("420", "ZipCodeOfRecipient_withoutCountryCode", 3, DataType.Alphanumeric, 20, true)
        Add("421", "ZipCodeOfRecipient_withCountryCode", 3, DataType.Alphanumeric, 12, true)
        Add("422", "BasisCountryOfTheWares_ISO3166Format", 3, DataType.Numeric, 3, false)
        Add("7001", "Nato Stock Number", 4, DataType.Numeric, 13, false)
        Add("8001", "RolesProducts", 4, DataType.Numeric, 14, false)
        Add("8002", "SerialNumberForMobilePhones", 4, DataType.Alphanumeric, 20, true)
        Add("8003", "GlobalReturnableAssetIdentifier", 4, DataType.Alphanumeric, 34, true)
        Add("8004", "GlobalIndividualAssetIdentifier", 4, DataType.Numeric, 30, true)
        Add("8005", "SalesPricePerUnit", 4, DataType.Numeric, 6, false)
        Add("8006", "IdentifikationOfAProductComponent", 4, DataType.Numeric, 18, false)
        Add("8007", "IBAN", 4, DataType.Alphanumeric, 30, true)
        Add("8008", "DataAndTimeOfManufacturing", 4, DataType.Numeric, 12, true)
        Add("8018", "GlobalServiceRelationNumber", 4, DataType.Numeric, 18, false)
        Add("8020", "NumberBillCoverNumber", 4, DataType.Alphanumeric, 25, false)
        Add("8100", "CouponExtendedCode_NSC_offerCcode", 4, DataType.Numeric, 10, false)
        Add("8101", "CouponExtendedCode_NSC_offerCcode_EndOfOfferCode", 4, DataType.Numeric, 14, false)
        Add("8102", "CouponExtendedCode_NSC", 4, DataType.Numeric, 6, false)
        Add("90", "InformationForBilateralCoordinatedApplications", 2, DataType.Alphanumeric, 30, true)
        Add("91", "Company specific", 2, DataType.Alphanumeric, 30, true)
        Add("92", "Company specific", 2, DataType.Alphanumeric, 30, true)
        //Add("93", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("94", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("95", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("96", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("97", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("98", "Company specific", 2, DataType.Alphanumeric, 30, true);
        //Add("99", "Company specific", 2, DataType.Alphanumeric, 30, true);
        minLengthOfAI = aiiDict.values.indexOf(aiiDict.values.minBy { it.LengthOfAI })
        maxLengthOfAI = aiiDict.values.indexOf(aiiDict.values.maxBy { it.LengthOfAI })
    }
}