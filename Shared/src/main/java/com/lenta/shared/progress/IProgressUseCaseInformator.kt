package com.lenta.shared.progress

import com.lenta.shared.interactor.UseCase

interface IProgressUseCaseInformator {
    fun <Params> getTitle(useCase: UseCase<Any, Params>): String
}