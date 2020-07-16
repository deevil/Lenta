package com.lenta.movement.exception

import com.lenta.shared.exception.Failure

class InfoFailure(
    val msg: String
) : Failure.FeatureFailure()