package com.lenta.bp12.platform.extention

import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.pojo.AlcoCodeInfo
import com.lenta.bp12.model.pojo.MarkTypeGroup
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz109V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz39V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz44V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz09V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz22V001
import com.lenta.shared.utilities.enumValueOrNull
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.orIfNull


fun ZmpUtz09V001.getProviderInfo(code: String): ProviderInfo? {
    val formattedCode = code.addZerosToStart(10)

    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_VENDORS.getWhere("VENDOR = \"$formattedCode\" LIMIT 1").firstOrNull()?.let { provider ->
        ProviderInfo(
                code = formattedCode,
                name = provider.vendorname
        )
    }
}

fun ZmpUtz39V001.getTaskTypeList(): List<TaskType> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.all.mapNotNull {
        TaskType(
                code = it.taskType.orEmpty(),
                description = it.annotation.orEmpty(),
                isDivBySection = it.divAbtnr.isSapTrue(),
                isDivByPurchaseGroup = it.divEkgrp.isSapTrue(),
                isDivByMark = it.isDivByMarkType.isSapTrue(),
                isDivByGis = it.isDivByGis.isSapTrue(),
                isDivByMinimalPrice = it.isDivByMinimalPrice.isSapTrue(),
                isDivByProvider = it.isDivByProvider.isSapTrue(),
                isDivByGoodType = it.isDivByMaterialType.isSapTrue()
        )
    }
}

fun ZmpUtz39V001.getTaskType(code: String): TaskType? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.getWhere("TASK_TYPE = \"$code\" LIMIT 1").firstOrNull()?.let {
        TaskType(
                code = it.taskType.orEmpty(),
                description = it.annotation.orEmpty(),
                isDivBySection = it.divAbtnr.isSapTrue(),
                isDivByPurchaseGroup = it.divEkgrp.isSapTrue(),
                isDivByMark = it.isDivByMarkType.isSapTrue(),
                isDivByGis = it.isDivByGis.isSapTrue(),
                isDivByMinimalPrice = it.isDivByMinimalPrice.isSapTrue(),
                isDivByProvider = it.isDivByProvider.isSapTrue(),
                isDivByGoodType = it.isDivByMaterialType.isSapTrue()
        )
    }
}

fun ZmpUtz44V001.getReturnReason(taskType: String, reasonCode: String): ReturnReason? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_REASONS.getWhere("TASK_TYPE = \"$taskType\" AND REASON = \"$reasonCode\" LIMIT 1").firstOrNull()?.let {
        ReturnReason(
                code = it.reason.orEmpty(),
                description = it.grtxt.orEmpty()
        )
    }
}

fun ZmpUtz44V001.getReturnReasonList(taskType: String): List<ReturnReason> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_REASONS.getWhere("TASK_TYPE = \"$taskType\"").map {
        ReturnReason(
                code = it.reason.orEmpty(),
                description = it.grtxt.orEmpty()
        )
    }
}

fun ZmpUtz22V001.getAlcoCodeInfoList(alcoCode: String): List<AlcoCodeInfo> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_ALCOD_LIST.getWhere("ZALCCOD = \"$alcoCode\"").map {
        AlcoCodeInfo(
                material = it.matnr.orEmpty(),
                code = it.zalccod.orEmpty()
        )
    }
}

fun ZfmpUtz48V001.getNameByMaterial(material: String): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_MATNR_LIST.getWhere("MATERIAL = \"$material\" LIMIT 1").firstOrNull()?.name
}


fun ZmpUtz109V001.isMarkTypeInDatabase(markType: String): Boolean {
    return localHelper_ET_MARK_GROUP.getWhere("ZMARKTYPE = \"$markType\" LIMIT 1").isEmpty().not()
}

fun ZmpUtz109V001.getMarkTypeGroupByMarkType(markType: String): MarkTypeGroup? {
    val cell = localHelper_ET_MARK_GROUP.getWhere("ZMARKTYPE = \"$markType\" LIMIT 1").firstOrNull()
    return cell?.let {
        MarkTypeGroup(
                name = it.markGroupName.orEmpty(),
                code = it.markGroupCode.orEmpty(),
                abbreviation = it.markGroupAbr.orEmpty(),
                markTypes = setOf()
        )
    }
}


fun ZmpUtz109V001.getMarkTypeGroups(): Set<MarkTypeGroup> {
    return localHelper_ET_MARK_GROUP.getAll().groupBy { Triple(it.markGroupName, it.markGroupCode, it.markGroupAbr) }
            .mapValues {
                it.value.map {
                    enumValueOrNull<MarkType>(it.markType.orEmpty()).orIfNull { MarkType.UNKNOWN }
                }
            }
            .mapTo(mutableSetOf()) {
                MarkTypeGroup(
                        name = it.key.first.orEmpty(),
                        code = it.key.second.orEmpty(),
                        abbreviation = it.key.third.orEmpty(),
                        markTypes = it.value.toSet()
                )
            }
}