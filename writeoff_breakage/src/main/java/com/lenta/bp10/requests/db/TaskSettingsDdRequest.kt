package com.lenta.bp10.requests.db

import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class TaskSettingsDbRequest
@Inject constructor(private val hyperHive: HyperHive) : UseCase<List<ZmpUtz29V001Rfc.ItemLocal_ET_TASK_TPS>, Nothing?>() {
    override suspend fun run(params: Nothing?): Either<Failure, List<ZmpUtz29V001Rfc.ItemLocal_ET_TASK_TPS>> {
        @Suppress("INACCESSIBLE_TYPE")
        return Either.Right(ZmpUtz29V001Rfc(hyperHive).localHelper_ET_TASK_TPS.all)
    }

}

