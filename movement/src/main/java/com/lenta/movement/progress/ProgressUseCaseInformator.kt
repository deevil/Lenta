package com.lenta.movement.progress

import android.content.Context
import com.lenta.movement.R
import com.lenta.movement.requests.network.SavePackagedExciseBoxesNetRequest
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ProgressUseCaseInformator @Inject constructor(
    private val context: Context,
    private val coreProgressUseCaseInformator: IProgressUseCaseInformator
) : IWriteOffProgressUseCaseInformator {

    override fun <Params> getTitle(useCase: UseCase<Any, Params>): String {
        return when (useCase) {
            is SavePackagedExciseBoxesNetRequest -> context.getString(R.string.save_to_sap_erp_msg)
            else -> coreProgressUseCaseInformator.getTitle(useCase)
        }
    }

}

interface IWriteOffProgressUseCaseInformator : IProgressUseCaseInformator