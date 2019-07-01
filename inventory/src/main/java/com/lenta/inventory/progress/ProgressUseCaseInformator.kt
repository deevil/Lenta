package com.lenta.inventory.progress

import android.content.Context
import com.lenta.inventory.R
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject


class ProgressUseCaseInformator @Inject constructor(private val coreProgressUseCaseInformator: IProgressUseCaseInformator, private val context: Context) : IInventoryProgressUseCaseInformator {
    override fun <Params> getTitle(useCase: UseCase<Any, Params>): String {
        coreProgressUseCaseInformator.getTitle(useCase).let {
            return if (it == context.getString(R.string.data_loading)) {
                //TODO Здесь должны быть заголовки для процессов инвентаризации
                /*when (useCase) {

                    else -> context.getString(R.string.data_loading)
                }*/
                it
            } else {
                it
            }
        }
    }
}

interface IInventoryProgressUseCaseInformator : IProgressUseCaseInformator