package com.lenta.bp9.features.transfer_goods_section

import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.shared.utilities.databinding.Evenable

data class TransferGoodsSectionItem(
        val number: String,
        val condition: String,
        val representative: String?,
        val ofGoods: String?,
        val sectionInfo: TaskSectionInfo,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class SectionPersonnelNumberInfo (
        val sectionNumber: String, //Номер секции
        val personnelNumber: String, //Табельный номоер
        val fio: String
)