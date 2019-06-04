package com.lenta.inventory.progress

import android.content.Context
import com.lenta.inventory.R
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ProgressUseCaseInformator @Inject constructor(private val context: Context) : IProgressUseCaseInformator {
    override fun <Params> getTitle(useCase: UseCase<Any, Params>): String {
        return when (useCase) {
            else -> context.getString(R.string.data_loading)
        }
    }
}