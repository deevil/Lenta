package com.lenta.bp12.model.pojo

import com.lenta.shared.utilities.BlockType

data class Block(
        val type: BlockType,
        val user: String,
        val ip: String
)