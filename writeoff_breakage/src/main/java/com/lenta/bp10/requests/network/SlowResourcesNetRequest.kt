package com.lenta.bp10.requests.network

import com.lenta.bp10.fmp.resources.slow.ZmpUtz22V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz25V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class SlowResourcesNetRequest @Inject constructor(private val hyperHive: HyperHive) : UseCase<Boolean, Nothing?>() {
    override suspend fun run(params: Nothing?): Either<Failure, Boolean> {
        var either = ZmpUtz22V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz25V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        either = ZmpUtz30V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
        if (either.isLeft) {
            return either
        }
        return either


    }

}

