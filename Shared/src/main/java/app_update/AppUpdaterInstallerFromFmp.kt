package app_update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.NotFoundAppUpdateFileError
import com.lenta.shared.fmp.files.FmpFilesStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.platform.navigation.CoreNavigator
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.lenta.shared.utilities.extentions.hhive.toEitherBoolean
import com.mobrun.plugin.api.HyperHive
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AppUpdaterInstallerFromFmp @Inject constructor(
        private val config: AppUpdaterConfig,
        private val context: Context,
        private val hyperHive: HyperHive,
        private val coreNavigator: CoreNavigator
) : AppUpdateInstaller {
    override suspend fun checkNeedAndHaveUpdate(codeVersion: Int?): Either<Failure, String> {
        @Suppress("DEPRECATION") val currentCodeVersion = context.packageManager
                .getPackageInfo(context.packageName, 0)?.versionCode ?: 0

        Logg.d { "currentCodeVersion: $currentCodeVersion" }

        if (codeVersion != null && currentCodeVersion >= codeVersion) {
            return Either.Right("")
        }

        val status = hyperHive.fileConnectorApi
                .directoryGet(dirName(), storageName, FmpFilesStatus::class.java).execute()

        Logg.d { "status result files: ${status?.result?.raw?.files}" }
        if (status.isNotBad()) {
            val files = status?.result?.raw?.files ?: emptyList()
            if (codeVersion == null) {
                return Either.Right(files.maxBy {
                    it.split("-").lastOrNull()?.replace(".apk", "")?.toIntOrNull() ?: 0
                }.orEmpty())
            }
            return files.find { it.endsWith("-$codeVersion.apk") }.let { fileName ->
                if (fileName.isNullOrBlank()) {
                    Either.Left(NotFoundAppUpdateFileError(codeVersion))
                } else {
                    Either.Right(fileName)
                }
            }
        }
        return status.toEitherBoolean().map { "" }.rightToLeft { Failure.WeighingError }
    }

    override suspend fun installUpdate(fileName: String): Either<Failure, Unit> {
        val serverPath: String = dirName() + fileName
        val localFilePath: String = createFile("${config.folderName}_update.apk")
                ?: return Either.Left(Failure.FileReadingError)
        var status = hyperHive.fileConnectorApi.fileGet(localFilePath, serverPath, storageName).execute()
        return status.toEitherBoolean().map {
            sendInstallUpdateIntentAndCloseApp(File(localFilePath))
        }
    }


    private fun sendInstallUpdateIntentAndCloseApp(localFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val apkUri: Uri = FileProvider.getUriForFile(context, config.applicationId + ".provider", localFile)
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            intent.data = apkUri
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        } else {
            val apkUri = Uri.fromFile(localFile)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        coreNavigator.finishApp()
    }

    private fun dirName(): String {
        return "./$rootFolderNameForUpdates/${config.folderName}/"
    }

    private fun createFile(name: String): String? {
        val file = File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS), name)
        try {
            if (file.exists() || file.createNewFile()) {
                return file.absolutePath
            }
        } catch (e: IOException) {
            Logg.e { "create file exception: $e" }
        }
        return null
    }

    companion object {
        private const val storageName = "updates"
        private const val rootFolderNameForUpdates = "app_updates"
    }
}


data class AppUpdaterConfig(
        /**
         * имя папки, где нужно искать файлы APK на сервере FMP
         */
        val folderName: String,
        val applicationId: String
)