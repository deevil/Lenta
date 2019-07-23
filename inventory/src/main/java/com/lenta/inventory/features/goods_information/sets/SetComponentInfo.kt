package com.lenta.inventory.features.goods_information.sets

data class SetComponentInfo(
        val materialNumber: String, // Номер товара
        val componentNumber: String, //Номер компонента
        val countComponent: String, //Количество вложенного (компонента в наборе)
        val uom: String //Базисная единица измерения
)