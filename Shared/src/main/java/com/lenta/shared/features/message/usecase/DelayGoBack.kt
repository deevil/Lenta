package com.lenta.shared.features.message.usecase

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.platform.navigation.ICoreNavigator
import kotlinx.coroutines.delay
import javax.inject.Inject

class DelayGoBack
@Inject constructor(val coreNavigator: ICoreNavigator) : UseCase<Boolean, Long>() {
    override suspend fun run(params: Long): Either<Failure, Boolean> {
        delay(params)
        coreNavigator.goBack()
        return Either.Right(true)
    }


}
