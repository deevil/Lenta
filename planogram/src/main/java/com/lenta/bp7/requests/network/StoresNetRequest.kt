package com.lenta.bp7.requests.network

import com.lenta.bp7.fmp.resources.storloc.ZmpUtz02V001
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.toEitherBoolean
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class StoresNetRequest
@Inject constructor(private val hyperHive: HyperHive, private val sessionInfo: ISessionInfo) : UseCase<Boolean, Nothing?>() {
    override suspend fun run(params: Nothing?): Either<Failure, Boolean> {
        val res = ZmpUtz02V001(hyperHive).newRequest()
                .addScalar(ZmpUtz02V001.LimitedScalarParameter.IV_PLANT(sessionInfo.market))
                .streamCallTable().execute()
        return res.toEitherBoolean()
    }
}

