package com.lenta.shared.features.login.usecase

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class Auth
@Inject constructor(private val hyperHive: HyperHive) : UseCase<Boolean, AuthParams>() {
    override suspend fun run(params: AuthParams): Either<Failure, Boolean> {
        Logg.d { "Thread: ${Thread.currentThread()}" }
        return hyperHive.authAPI.auth(params.login, params.password, false).execute().toEither()
    }
}

data class AuthParams(val login: String, val password: String)