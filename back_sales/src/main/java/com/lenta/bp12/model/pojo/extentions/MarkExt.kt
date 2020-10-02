package com.lenta.bp12.model.pojo.extentions

import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkInfo

fun List<Mark>.isAnyAlreadyIn(inputMarks: List<Mark>): Boolean {
    return this.any { tempMark ->
        inputMarks.any {
            tempMark.number == it.number
        }
    }
}

fun List<Mark>.isAllAlreadyIn(inputMarks: List<Mark>): Boolean {
    return this.all { tempMark ->
        inputMarks.all {
            tempMark.number == it.number
        }
    }
}

fun List<MarkInfo>.mapToMarkList(foundGood: Good): List<Mark> {
    return this.map {
        Mark(
                number = it.markNumber.orEmpty(),
                packNumber = it.cartonNumber.orEmpty(),
                boxNumber = it.boxNumber.orEmpty(),
                maxRetailPrice = foundGood.maxRetailPrice
        )
    }
}
