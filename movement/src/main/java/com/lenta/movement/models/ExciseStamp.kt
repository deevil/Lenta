package com.lenta.movement.models

import com.lenta.shared.models.core.ExciseStamp

class ExciseStamp(
    code: String,
    materialNumber: String,
    val manufacturerName: String,
    val dateOfPour: String
) : ExciseStamp(materialNumber, code)