package com.lenta.shared.account

import com.lenta.shared.di.AppScope
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

@AppScope
class Authenticator
@Inject constructor(private val hyperHive: HyperHive) : IAuthenticator {
    override fun isAuthorized() = hyperHive.authAPI.isAuthorized
}


interface IAuthenticator {
    fun isAuthorized(): Boolean
}