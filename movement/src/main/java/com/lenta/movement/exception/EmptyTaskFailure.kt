package com.lenta.movement.exception

import com.lenta.shared.exception.Failure

class EmptyTaskFailure (
        val msg: String
) : Failure.FeatureFailure()