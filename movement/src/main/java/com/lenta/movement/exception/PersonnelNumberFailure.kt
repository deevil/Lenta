package com.lenta.movement.exception

import com.lenta.shared.exception.Failure

class PersonnelNumberFailure (
        val msg: String
) : Failure.FeatureFailure()