package com.lenta.bp14.platform.extentions

import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.toCheckListGoodInfo(): CheckListGoodInfo? {
    return CheckListGoodInfo(
            material = material,
            name = name,
            buom = buom
    )
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.toWorkListGoodInfo(): WorkListGoodInfo? {
    return WorkListGoodInfo(
            material = material,
            name = name,
            unitsCode = buom,
            goodGroup = matkl,
            purchaseGroup = ekgrp,
            shelfLife = mhdhbDays,
            remainingShelfLife = mhdrzDays,
            matrixType = matrType,
            section = abtnr,
            isAlcohol = isAlco,
            isMark = isMark,
            healthFood = isHf,
            novelty = isNew
    )
}

data class CheckListGoodInfo(
        val material: String,
        val name: String,
        val buom: String
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
        val isAlcohol: String,
        val isMark: String,
        val healthFood: String,
        val novelty: String
)