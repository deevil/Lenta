package com.lenta.bp10.requests.db

import com.lenta.bp10.fmp.ZfmpUtzWob01V001
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PermissionsDbRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<List<ZfmpUtzWob01V001.ItemLocal_ET_WERKS>, Nothing?>() {
    override suspend fun run(params: Nothing?): Either<Failure, List<ZfmpUtzWob01V001.ItemLocal_ET_WERKS>> {
        return Either.Right(ZfmpUtzWob01V001(hyperHive).localHelper_ET_WERKS.all)
    }

}

