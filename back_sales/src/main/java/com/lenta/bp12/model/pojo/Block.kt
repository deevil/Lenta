package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.BlockType

data class Block(
        val type: BlockType,
        val user: String,
        val ip: String
)