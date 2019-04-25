package com.lenta.bp10.progress

import android.content.Context
import com.lenta.bp10.R
import com.lenta.bp10.requests.network.PersonnelNumberNetRequest
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ProgressUseCaseInformator @Inject constructor(val context: Context) : IProgressUseCaseInformator {
    override fun <Params> getTitle(useCase: UseCase<Any, Params>): String {
        return when (useCase) {
            is PersonnelNumberNetRequest -> context.getString(R.string.loading_tab_number)
            else -> context.getString(R.string.data_loading)
        }
    }
}