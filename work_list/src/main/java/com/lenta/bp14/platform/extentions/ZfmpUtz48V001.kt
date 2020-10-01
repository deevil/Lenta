package com.lenta.bp14.platform.extentions

import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.toCheckListGoodInfo(): CheckListGoodInfo? {
    return if (material == null && name == null && buom == null) {
        null
    } else {
        CheckListGoodInfo(
                material = material.orEmpty(),
                name = name.orEmpty(),
                unitsCode = buom.orEmpty()
        )
    }
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.toWorkListGoodInfo(): WorkListGoodInfo? {
    return if (material == null && name == null && buom == null) {
        null
    } else {
        return WorkListGoodInfo(
                material = material.orEmpty(),
                name = name.orEmpty(),
                unitsCode = buom.orEmpty(),
                goodGroup = matkl.orEmpty(),
                purchaseGroup = ekgrp.orEmpty(),
                shelfLife = mhdhbDays ?: 0,
                remainingShelfLife = mhdrzDays ?: 0,
                matrixType = matrType.orEmpty(),
                section = abtnr.orEmpty(),
                isExcise = isExc.orEmpty(),
                isAlcohol = isAlco.orEmpty(),
                isMark = isMark.orEmpty(),
                isRusWine = isRusWine,
                healthFood = isHf.orEmpty(),
                novelty = isNew.orEmpty()
        )
    }
}

data class CheckListGoodInfo(
        val material: String,
        val name: String,
        val unitsCode: String
)

data class WorkListGoodInfo(
        val material: String,
        val name: String,
        val unitsCode: String,
        var goodGroup: String,
        var purchaseGroup: String,
        val shelfLife: Int,
        val remainingShelfLife: Int,
        val matrixType: String,
        val section: String,
        val isExcise: String,
        val isAlcohol: String,
        val isRusWine: String?, // todo Что это такое, оно реально используется?
        val isMark: String,
        val healthFood: String,
        val novelty: String
)