package com.lenta.inventory.progress

import android.content.Context
import com.lenta.inventory.R
import com.lenta.inventory.requests.network.StorePlaceLockNetRequest
import com.lenta.inventory.requests.network.TaskContentNetRequest
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject


class ProgressUseCaseInformator @Inject constructor(private val coreProgressUseCaseInformator: IProgressUseCaseInformator, private val context: Context) : IInventoryProgressUseCaseInformator {
    override fun <Params> getTitle(useCase: UseCase<Any, Params>): String {
        coreProgressUseCaseInformator.getTitle(useCase).let {
            return if (it == context.getString(R.string.data_loading)) {
                when (useCase) {
                    is StorePlaceLockNetRequest -> context.getString(R.string.store_place_lock_unlock)
                    is TaskContentNetRequest -> context.getString(R.string.task_content_loading)
                    else -> context.getString(R.string.data_loading)
                }
            } else {
                it
            }
        }
    }
}

interface IInventoryProgressUseCaseInformator : IProgressUseCaseInformator