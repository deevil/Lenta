package com.lenta.shared.features.message.usecase

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.platform.navigation.ICoreNavigator
import kotlinx.coroutines.delay
import javax.inject.Inject

class DelayGoBack
@Inject constructor(val coreNavigator: ICoreNavigator) : UseCase<Boolean, Nothing?>() {
    private val timeDelayInMillis = 3_000L
    override suspend fun run(params: Nothing?): Either<Failure, Boolean> {
        delay(timeDelayInMillis)
        coreNavigator.goBack()
        return Either.Right(true)
    }


}
