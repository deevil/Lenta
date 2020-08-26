package com.lenta.bp16.request

import com.lenta.bp16.model.movement.params.MovementParams
import com.lenta.bp16.model.movement.result.MovementResult
import com.lenta.bp16.repository.IMovementRepository
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class MovementNetRequest @Inject constructor(
        private val movementRepository: IMovementRepository
) : UseCase<MovementResult, MovementParams> {
    override suspend fun run(params: MovementParams): Either<Failure, MovementResult> {
        return movementRepository.sendMovementInfo(params)
    }
}