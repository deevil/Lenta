package com.lenta.movement.exception

import com.lenta.shared.exception.Failure

data class ThrowableFailure(val e: Throwable) : Failure.FeatureFailure()
