package com.lenta.bp10.requests.network

import com.lenta.bp10.fmp.resources.fast.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class FastResourcesNetRequest @Inject constructor(private val hyperHive: HyperHive) : UseCase<Boolean, Nothing?>() {
    override suspend fun run(params: Nothing?): Either<Failure, Boolean> {
        var either = ZmpUtz07V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz14V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz26V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz31V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz32V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz33V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz34V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz36V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz38V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        return either

    }

}

