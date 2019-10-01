package com.lenta.shared.requests.db

import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject
//Nothing?
class PrinterChangeDBRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<List<ZmpUtz26V001.ItemLocal_ET_PRINTERS>, String> {
    override suspend fun run(params: String): Either<Failure, List<ZmpUtz26V001.ItemLocal_ET_PRINTERS>> {
        @Suppress("INACCESSIBLE_TYPE")
        return Either.Right(ZmpUtz26V001(hyperHive).localHelper_ET_PRINTERS.getWhere("WERKS = \"$params\""))
    }

}