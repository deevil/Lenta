package com.lenta.shared.requests.network

import com.lenta.shared.analytics.IAnalytics
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.toEitherBoolean
import com.lenta.shared.utilities.runIfRelease
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class Auth
@Inject constructor(private val hyperHive: HyperHive, private val analytics: IAnalytics) : UseCase<Boolean, AuthParams>() {
    override suspend fun run(params: AuthParams): Either<Failure, Boolean> {
        return hyperHive.authAPI.auth(params.login, params.password, true).execute().toEitherBoolean().apply {
            if (this.isRight) {
                runIfRelease {
                    analytics.init()
                    analytics.sendLogs()
                }
            }

        }
    }
}

data class AuthParams(val login: String, val password: String)