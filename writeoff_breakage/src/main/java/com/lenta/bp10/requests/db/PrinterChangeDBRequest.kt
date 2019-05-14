package com.lenta.bp10.requests.db

import com.lenta.bp10.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PrinterChangeDBRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<List<ZmpUtz26V001.ItemLocal_ET_PRINTERS>, Nothing?>() {
    override suspend fun run(params: Nothing?): Either<Failure, List<ZmpUtz26V001.ItemLocal_ET_PRINTERS>> {
        @Suppress("INACCESSIBLE_TYPE")
        return Either.Right(ZmpUtz26V001(hyperHive).localHelper_ET_PRINTERS.all)
    }

}