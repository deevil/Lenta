package app_update

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either

interface AppUpdateInstaller {
    suspend fun checkNeedAndHaveUpdate(codeVersion: Int) : Either<Failure, String>
    suspend fun installUpdate(fileName: String) : Either<Failure, Unit>

}