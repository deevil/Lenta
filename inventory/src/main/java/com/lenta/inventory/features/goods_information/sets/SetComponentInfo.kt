package com.lenta.inventory.features.goods_information.sets

import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo

data class SetComponentInfo(
        val setNumber: String, // Номер товара
        val number: String, //Номер компонента
        val name: String, //Наименование компонента
        val ean: List<String?>, //ШК компонента
        val count: String, //Количество вложенного (компонента в наборе)
        val uom: Uom, //Базисная единица измерения
        val matrixType: MatrixType,
        val sectionId: String,
        val typeProduct: ProductType,
        val placeCode: String
)