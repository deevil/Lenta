package com.lenta.bp14.di

import android.content.Context
import com.google.gson.Gson
import com.lenta.bp14.models.GeneralTaskManager
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_list.ICheckListTask
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.models.data.TaskManager
import com.lenta.bp14.models.general.GeneralRepo
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.VibrateHelper
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.navigation.ScreenNavigator
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.bp14.platform.sound.SoundPlayer
import com.lenta.bp14.repos.IRepoInMemoryHolder
import com.lenta.bp14.repos.RepoInMemoryHolder
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.progress.IProgressUseCaseInformator
import dagger.Module
import dagger.Provides

@Module
class AppModule {


    @Provides
    @AppScope
    internal fun provideScreenNavigator(
            context: Context,
            iCoreNavigator: ICoreNavigator,
            foregroundActivityProvider: ForegroundActivityProvider,
            authenticator: IAuthenticator,
            progressUseCaseInformator: IProgressUseCaseInformator
    ): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator, progressUseCaseInformator)
    }


    @Provides
    @AppScope
    internal fun provideRepoInMEmoryHolder(): IRepoInMemoryHolder {
        return RepoInMemoryHolder()
    }

    @Provides
    @AppScope
    internal fun provideTaskManager(): TaskManager {
        return TaskManager()
    }

    @Provides
    @AppScope
    internal fun provideCheckPriceTaskManager(
            timeMonitor: ITimeMonitor,
            gson: Gson,
            soundPlayer: ISoundPlayer,
            vibrateHelper: IVibrateHelper): CheckPriceTaskManager {
        return CheckPriceTaskManager(
                timeMonitor = timeMonitor,
                gson = gson,
                soundPlayer = soundPlayer,
                vibrateHelper = vibrateHelper
        )
    }

    @Provides
    @AppScope
    internal fun provideCheckListTaskManager(timeMonitor: ITimeMonitor, gson: Gson): CheckListTaskManager {
        return CheckListTaskManager(timeMonitor = timeMonitor, gson = gson)
    }

    @Provides
    @AppScope
    internal fun provideWorkListTaskManager(timeMonitor: ITimeMonitor, gson: Gson): WorkListTaskManager {
        return WorkListTaskManager(timeMonitor = timeMonitor, gson = gson)
    }

    @Provides
    @AppScope
    internal fun provideGeneralRepo(): IGeneralRepo {
        return GeneralRepo()
    }

    @Provides
    @AppScope
    internal fun provideGeneralTaskManager(
            checkPriceTaskManager: CheckPriceTaskManager,
            checkListTaskManager: CheckListTaskManager,
            workListTaskManager: WorkListTaskManager,
            timeMonitor: ITimeMonitor): IGeneralTaskManager {
        return GeneralTaskManager(
                checkPriceTaskManager = checkPriceTaskManager,
                checkListTaskManager = checkListTaskManager,
                workListTaskManager = workListTaskManager,
                timeMonitor = timeMonitor
        )
    }

    @Provides
    internal fun provideCheckPriceTask(checkPriceTaskManager: CheckPriceTaskManager): ICheckPriceTask {
        return checkPriceTaskManager.getTask()!!
    }

    @Provides
    internal fun provideCheckListTask(checkListTaskManager: CheckListTaskManager): ICheckListTask {
        return checkListTaskManager.getTask()!!
    }

    @Provides
    internal fun provideWorkListTask(workListTaskManager: WorkListTaskManager): WorkListTask {
        return workListTaskManager.getTask()!!
    }

    @Provides
    internal fun provideSoundPlayer(context: Context): ISoundPlayer {
        return SoundPlayer(context)
    }

    @Provides
    internal fun provideVibrateHelper(context: Context): IVibrateHelper {
        return VibrateHelper(context)
    }

}