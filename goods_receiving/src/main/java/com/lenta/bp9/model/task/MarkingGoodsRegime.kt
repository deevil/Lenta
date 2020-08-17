package com.lenta.bp9.model.task

import com.lenta.bp9.features.goods_list.SearchProductDelegate

enum class MarkingGoodsRegime {
    UomStWithoutBoxes,
    UomStWithBoxes,
    Unknown
}


fun getMarkingGoodsRegime(taskManager: IReceivingTaskManager, productInfo: TaskProductInfo): MarkingGoodsRegime {
    val isExciseStampsNotEmpty =
            taskManager
                    .getReceivingTask()
                    ?.getProcessedExciseStamps()
                    ?.size
                    ?: 0 > 0

    //условие для: MARK.ППП. Табак. Карточка товара. Марочный учет. ЕИЗ=ШТ. Признак IS_USE_ALTERN_MEINS не установлен. Таблица task_mark не пустая. https://trello.com/c/NGsFfWgB
    if (isExciseStampsNotEmpty
            && productInfo.markType == MarkType.Tobacco
            && productInfo.isMarkFl
            && !productInfo.isCountingBoxes
            && productInfo.purchaseOrderUnits.code == SearchProductDelegate.UNIT_CODE_ST) {
        return MarkingGoodsRegime.UomStWithoutBoxes
    }

    //условие для: MARK.ППП. Табак. Карточка товара. Марочный учет. ЕИЗ=ШТ. Признак IS_USE_ALTERN_MEINS установлен. Таблица task_mark не пустая. https://trello.com/c/vl9wQg0Y
    if (isExciseStampsNotEmpty
            && productInfo.markType == MarkType.Tobacco
            && productInfo.isMarkFl
            && productInfo.isCountingBoxes
            && productInfo.purchaseOrderUnits.code == SearchProductDelegate.UNIT_CODE_ST) {
        return MarkingGoodsRegime.UomStWithBoxes
    }

    return MarkingGoodsRegime.Unknown
}