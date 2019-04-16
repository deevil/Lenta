package com.lenta.bp10.features.auth

import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

@AppScope
class Authenticator
@Inject constructor(private val hyperHive: HyperHive) : IAuthenticator {
    override fun isAuthorized() = hyperHive.authAPI.isAuthorized
}