package com.lenta.shared.progress

import android.content.Context
import com.lenta.shared.R
import com.lenta.shared.requests.network.PersonnelNumberNetRequest
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import javax.inject.Inject

class CoreProgressUseCaseInformator @Inject constructor(private val context: Context) : IProgressUseCaseInformator {
    override fun <Params> getTitle(useCase: UseCase<Any, Params>): String {
        return when (useCase) {
            is PersonnelNumberNetRequest -> context.getString(R.string.loading_tab_number)
            is ScanInfoRequest -> context.getString(R.string.product_search)
            else -> context.getString(R.string.data_loading)
        }
    }
}