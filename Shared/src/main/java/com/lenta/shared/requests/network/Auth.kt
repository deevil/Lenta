package com.lenta.shared.requests.network

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.lenta.shared.utilities.extentions.hhive.toEitherBoolean
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class Auth
@Inject constructor(private val hyperHive: HyperHive) : UseCase<Boolean, AuthParams>() {
    override suspend fun run(params: AuthParams): Either<Failure, Boolean> {
        return hyperHive.authAPI.auth(params.login, params.password, true).execute().toEitherBoolean()
    }
}

data class AuthParams(val login: String, val password: String)