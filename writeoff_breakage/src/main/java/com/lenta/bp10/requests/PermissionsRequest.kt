package com.lenta.bp10.requests

import com.lenta.bp10.fmp.ZfmpUtzWob01V001
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PermissionsRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<Boolean, PermissionsParams>() {
    override suspend fun run(params: PermissionsParams): Either<Failure, Boolean> {
        //TODO (DB) нужно добавить поддержку логина пользователя когда доработают ФМ модуль
        return ZfmpUtzWob01V001(hyperHive).newRequest().streamCallAuto().execute().toEither()
    }
}

data class PermissionsParams(val login: String)