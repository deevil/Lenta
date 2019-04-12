package com.lenta.shared.account

interface IAuthenticator {
    fun isAuthorized(): Boolean
}