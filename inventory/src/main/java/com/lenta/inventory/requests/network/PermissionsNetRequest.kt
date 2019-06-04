package com.lenta.inventory.requests.network

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PermissionsRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<Boolean, PermissionsParams>() {
    override suspend fun run(params: PermissionsParams): Either<Failure, Boolean> {
        //TODO (DB) нужно добавить поддержку логина пользователя когда доработают ФМ модуль
        //return ZfmpUtzWob01V001(hyperHive).newRequest().streamCallAuto().execute().toEitherBoolean()
        return Either.Right(true)
    }
}

data class PermissionsParams(val login: String)