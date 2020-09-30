package com.lenta.bp15.model.pojo

data class Good(
        val material: String,
        val planQuantity: Int,
        val markType: String,
        val marks: List<Mark> = mutableListOf()
)