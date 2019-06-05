package com.lenta.bp10.progress

import android.content.Context
import com.lenta.bp10.R
import com.lenta.shared.requests.network.PersonnelNumberNetRequest
import com.lenta.bp10.requests.network.PrintTaskNetRequest
import com.lenta.bp10.requests.network.ProductInfoNetRequest
import com.lenta.bp10.requests.network.SendWriteOffReportRequest
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ProgressUseCaseInformator @Inject constructor(private val context: Context) : IProgressUseCaseInformator {
    override fun <Params> getTitle(useCase: UseCase<Any, Params>): String {
        return when (useCase) {
            is PersonnelNumberNetRequest -> context.getString(R.string.loading_tab_number)
            is SendWriteOffReportRequest -> context.getString(R.string.sending_report_data)
            is ProductInfoNetRequest -> context.getString(R.string.loading_material_info)
            is PrintTaskNetRequest -> context.getString(R.string.sending_data_to_print)
            else -> context.getString(R.string.data_loading)
        }
    }
}