package com.lenta.bp12.platform.extention

import android.text.Editable
import com.lenta.bp12.R
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.platform.ONE_QUANTITY_IN_FLOAT
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.request.ScanInfoResult
import com.lenta.bp12.request.pojo.CreateTaskBasketInfo
import com.lenta.bp12.request.pojo.EanInfo
import com.lenta.bp12.request.pojo.TaskInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkRequestStatus
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.enumValueOrNull
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.getConvertedQuantity
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.getDateFromString
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

fun GoodKind.getDescriptionResId(): Int {
    return when (this) {
        GoodKind.COMMON -> R.string.common_product
        GoodKind.ALCOHOL -> R.string.alcohol
        GoodKind.EXCISE -> R.string.excise_alcohol
        GoodKind.MARK -> R.string.marked_good
        GoodKind.VET -> R.string.vet_good
    }
}

fun GoodInfoResult.getGoodKind(): GoodKind {
    val isAlcohol = this.materialInfo?.isAlcohol.isSapTrue()
    val isExcise = this.materialInfo?.isExcise.isSapTrue()
    val isVet = this.materialInfo?.isVet.isSapTrue()
    val isMark = this.materialInfo?.markType.orEmpty().isNotEmpty()

    return when {
        isExcise -> GoodKind.EXCISE
        isAlcohol -> GoodKind.ALCOHOL
        isMark -> GoodKind.MARK
        isVet -> GoodKind.VET
        else -> GoodKind.COMMON
    }
}

fun GoodInfoResult.getMarkType(): MarkType {
    val markTypeString = materialInfo?.markType.orEmpty()
    return enumValueOrNull<MarkType>(markTypeString).orIfNull { MarkType.UNKNOWN }
}

fun MarkCartonBoxGoodInfoNetRequestResult.getMarkStatus(): MarkStatus {
    return when (this.markStatus) {
        MarkRequestStatus.MARK_FOUND -> MarkStatus.GOOD_MARK

        MarkRequestStatus.MARK_NOT_FOUND_IN_TASK,
        MarkRequestStatus.MARK_NOT_FOUND_OR_PROBLEMATIC,
        MarkRequestStatus.MARK_OF_DIFFERENT_GOOD
        -> MarkStatus.BAD_MARK

        MarkRequestStatus.CARTON_FOUND_OR_GRAYZONE -> MarkStatus.GOOD_CARTON

        MarkRequestStatus.CARTON_INCOMPLETE,
        MarkRequestStatus.CARTON_NOT_FOUND,
        MarkRequestStatus.CARTON_NOT_FOUND_IN_TASK,
        MarkRequestStatus.CARTON_OF_DIFFERENT_GOOD,
        MarkRequestStatus.CARTON_OLD,
        MarkRequestStatus.CARTON_NOT_SAME_IN_SYSTEM
        -> MarkStatus.BAD_CARTON

        MarkRequestStatus.BOX_FOUND -> MarkStatus.GOOD_BOX

        MarkRequestStatus.BOX_NOT_FOUND,
        MarkRequestStatus.BOX_OF_DIFFERENT_GOOD,
        MarkRequestStatus.BOX_INCOMPLETE,
        MarkRequestStatus.BOX_NOT_FOUND_IN_TASK,
        MarkRequestStatus.BOX_NOT_SAME_IN_SYSTEM
        -> MarkStatus.BAD_BOX

        else -> MarkStatus.UNKNOWN
    }
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.getGoodKind(): GoodKind {
    val isAlcohol = this.isAlco.isSapTrue()
    val isExcise = this.isExc.isSapTrue()
    val isMark = this.markType.orEmpty().isNotEmpty()

    return when {
        isExcise -> GoodKind.EXCISE
        isAlcohol -> GoodKind.ALCOHOL
        isMark -> GoodKind.MARK
        else -> GoodKind.COMMON
    }
}

fun GoodInfoResult.getControlType(): ControlType {
    val isVet = this.materialInfo?.isVet.isSapTrue()
    val isAlcohol = this.materialInfo?.isAlcohol.isSapTrue()
    val isMark = this.getMarkType() != MarkType.UNKNOWN

    return when {
        isMark -> ControlType.MARK
        !isAlcohol && !isVet -> ControlType.COMMON
        isAlcohol && !isVet -> ControlType.ALCOHOL
        !isAlcohol && isVet -> ControlType.VET
        isMark -> ControlType.MARK
        else -> ControlType.UNKNOWN
    }
}

fun TaskInfo.getControlTypes(): Set<ControlType> {
    val setOfControlTypes = mutableSetOf<ControlType>()

    setOfControlTypes add ControlType.VET ifTrue isVet.isSapTrue()
    setOfControlTypes add ControlType.ALCOHOL ifTrue isAlco.isSapTrue()
    setOfControlTypes add ControlType.COMMON ifTrue isUsual.isSapTrue()
    setOfControlTypes add ControlType.MARK ifTrue isMark.isSapTrue()

    return setOfControlTypes
}

fun CreateTaskBasketInfo.getControlType(): ControlType {
    val isVet = this.isVet.isSapTrue()
    val isAlcohol = this.isAlcohol.isSapTrue()
    val isMark = this.isMark.isSapTrue()
    val isCommon = this.isCommon.isSapTrue()

    return when {
        isMark -> ControlType.MARK
        isCommon -> ControlType.COMMON
        isAlcohol -> ControlType.ALCOHOL
        isVet -> ControlType.VET
        else -> ControlType.UNKNOWN
    }
}
/**
 * Если товар обычный то удаляет второй минус
 * если Алко то первый
 * Использовать вместе с
 * TextViewBindingAdapter.AfterTextChanged
 * fun afterTextChanged(s: Editable?)
 * */
fun Editable?.resolveMinuses(isGoodCommon: Boolean): String {
    return if (isGoodCommon) {
        this.toString().returnWithNoMinuses(Constants.STRING_WITH_ONLY_ONE_MINUS_IN_BEGINNING_PATTERN)
    } else {
        this.toString().returnWithNoMinuses(Constants.STRING_WITH_ONLY_DIGITS_PATTERN)
    }
}

/**
 * Метод проверяет по регулярке есть ли минус в строке
 * */

fun String.returnWithNoMinuses(pattern: String): String {
    val regex = Regex(pattern)
    val quantity = this
    return if (!quantity.matches(regex)) {
        quantity.deleteMinus()
    } else {
        quantity
    }
}

fun TaskType.isWholesaleType(): Boolean {
    return this.code == TypeCode.WHOLESALE.code
}

fun ScanCodeInfo.getConvertedQuantityString(divider: Double): String {
    val converted = if (weight > 0.0) {
        getConvertedQuantity(divider)
    } else {
        ZERO_QUANTITY
    }
    return converted.dropZeros()
}

fun Float.dropZeros(): String {
    return if (this == this.toLong().toFloat())
        String.format("%d", this.toLong())
    else
        String.format("%s", this)
}

fun ScanInfoResult.getParts(good: Good, date: String, providerCode: String, producerCode: String): List<Part> {
    val formattedDate = try {
        getDateFromString(date, Constants.DATE_FORMAT_dd_mm_yyyy)
    } catch (e: RuntimeException) {
        Date()
    }

    return this.parts?.map { partFromServer ->
        Part(
                number = partFromServer.partNumber.orEmpty(),
                material = good.material,
                providerCode = providerCode,
                producerCode = producerCode,
                date = formattedDate
        ).apply {
            quantity = partFromServer.quantity?.toDoubleOrNull().orIfNull { ZERO_QUANTITY }
        }
    }.orEmpty()
}

suspend fun ZmpUtz25V001.getEanMapByMaterialUnits(material: String, unitsCode: String): MutableMap<String, Float> {
    @Suppress("INACCESSIBLE_TYPE")
    return withContext(Dispatchers.IO) {
        localHelper_ET_EANS.getWhere("MATERIAL = \"$material\" AND UOM = \"${unitsCode.toUpperCase(Locale.getDefault())}\"")
                .associateByTo(
                        destination = mutableMapOf<String, Float>(),
                        keySelector = { it.ean.orEmpty() },
                        valueTransform = { cell ->
                            cell.umrez?.let { umrez ->
                                cell.umren?.let { umren ->
                                    (umrez.toFloat() / umren.toFloat()).takeIf { cell.uom == Uom.DATA_KAR }
                                }
                            } ?: ONE_QUANTITY_IN_FLOAT
                        }
                )
    }
}


fun <T> MutableCollection<T>.addIf(predicate: Boolean, whatToAdd: () -> T) {
    if (predicate) this.add(whatToAdd())
}

infix fun <T> MutableCollection<T>.add(whatToAdd: T): Holder<T> {
    return Holder(whatToAdd, this)
}

infix fun <T> Holder<T>.ifTrue(predictValue: Boolean) {
    if (predictValue) {
        this.who.add(this.value)
    }
}

data class Holder<T>(val value: T, val who: MutableCollection<T>)

fun EanInfo?.getQuantityForBox(): Float {
    return this?.let {
        it.umrez?.toFloatOrNull()?.let { umrez ->
            it.umren?.toFloatOrNull()?.let { umren ->
                (umrez / umren).takeIf { unitCode == Uom.DATA_KAR }
            }
        }
    } ?: ONE_QUANTITY_IN_FLOAT
}