/**
 * Copyright (C) 2018 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lenta.shared.interactor

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means than any use
 * case in the application should implement this contract).
 *
 * By convention each [UseCase] implementation will execute its job in a background thread
 * (kotlin coroutine) and will post the result in the UI thread.
 */
interface UseCase<out Type, in Params> where Type : Any {

    suspend fun run(params: Params): Either<Failure, Type>

    suspend operator fun invoke(params: Params): Either<Failure, Type> {
        return withContext(Dispatchers.IO) {
            return@withContext run(params)
        }
    }

}


