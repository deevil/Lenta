package com.lenta.bp12.model.pojo

data class Properties(
        val type: String, // Сокращенное название типа задачи
        val description: String, // Полное название задачи
        val isDivBySection: Boolean, // Деление по секциям
        val isDivByPurchaseGroup: Boolean // Деление по группе закупок
)