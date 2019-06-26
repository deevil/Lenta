package com.lenta.bp10.requests.network

import com.lenta.bp10.fmp.resources.permissions.ZfmpUtzWob01V001
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.toEitherBoolean
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PermissionsRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<Boolean, PermissionsParams>() {
    override suspend fun run(params: PermissionsParams): Either<Failure, Boolean> {
        return ZfmpUtzWob01V001(hyperHive).newRequest()
                .addScalar(ZfmpUtzWob01V001.LimitedScalarParameter.IV_USER(params.login))
                .streamCallAuto().execute().toEitherBoolean()
    }
}

data class PermissionsParams(val login: String)