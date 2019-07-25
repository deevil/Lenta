package com.lenta.shared.features.message.usecase

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.platform.navigation.ICoreNavigator
import kotlinx.coroutines.delay
import javax.inject.Inject

class DelayGoBack
@Inject constructor(val coreNavigator: ICoreNavigator) : UseCase<Boolean, GoBackParams>() {
    override suspend fun run(params: GoBackParams): Either<Failure, Boolean> {
        delay(params.timeInMillis)
        coreNavigator.goBackWithResultCode(params.codeForBackResult)
        return Either.Right(true)
    }


}


data class GoBackParams(
        val timeInMillis: Long,
        val codeForBackResult: Int? = null
)
