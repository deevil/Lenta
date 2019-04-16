package com.lenta.shared.features.message.usecase

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.platform.navigation.IGoBackNavigator
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.delay
import javax.inject.Inject

class DelayGoBack
@Inject constructor(val goBackNavigator: IGoBackNavigator) : UseCase<Boolean, Nothing?>() {
    private val timeDelayInMillis = 3_000L
    override suspend fun run(params: Nothing?): Either<Failure, Boolean> {
        delay(timeDelayInMillis)
        Logg.d { "Thread.currentThread(): ${Thread.currentThread()}" }
        goBackNavigator.goBack()
        return Either.Right(true)
    }


}
