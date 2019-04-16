package com.lenta.shared.features.login

fun isEnterEnabled(isFieldsValid: Boolean?, inProgress: Boolean?): Boolean {
    return isFieldsValid ?: false && !(inProgress ?: false)
}

fun isValidLoginFields(login: String?, password: String?): Boolean {
    return !login.isNullOrEmpty() && !password.isNullOrEmpty()
}