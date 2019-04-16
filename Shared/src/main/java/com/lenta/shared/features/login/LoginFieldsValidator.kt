package com.lenta.shared.features.login

class LoginFieldsValidator : ITwoFieldsValidator {
    override fun isValid(login: String?, password: String?): Boolean {
        if (login == null || password == null || login.isNullOrEmpty() || password.isNullOrEmpty()) {
            return false
        }
        return true
    }

}

interface ITwoFieldsValidator {
    fun isValid(login: String?, password: String?): Boolean
}