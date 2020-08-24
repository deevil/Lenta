package com.lenta.bp9.features.goods_information.marking

import com.lenta.bp9.model.task.TaskBlockDiscrepancies

data class MarkingBlocksDiscrepanciesInfo (
        val blockDiscrepancies: TaskBlockDiscrepancies,
        val isGtinControlPassed: Boolean
)