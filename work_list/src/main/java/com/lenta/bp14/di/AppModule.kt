package com.lenta.bp14.di

import app_update.AppUpdateInstaller
import app_update.AppUpdaterConfig
import app_update.AppUpdaterInstallerFromFmp
import com.lenta.bp14.BuildConfig.APPLICATION_ID
import com.lenta.bp14.models.GeneralTaskManager
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.IPersistTaskData
import com.lenta.bp14.models.PersistTaskData
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.check_price.PriceInfoParser
import com.lenta.bp14.models.general.GeneralRepo
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITasksSearchHelper
import com.lenta.bp14.models.general.TasksSearchHelper
import com.lenta.bp14.models.print.BigDatamaxPrint
import com.lenta.bp14.models.print.BigDatamaxPrintImpl
import com.lenta.bp14.models.print.IPrintTask
import com.lenta.bp14.models.print.PrintTask
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.VibrateHelper
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.navigation.ScreenNavigator
import com.lenta.bp14.platform.resource.IResourceManager
import com.lenta.bp14.platform.resource.ResourceManager
import com.lenta.bp14.repos.IRepoInMemoryHolder
import com.lenta.bp14.repos.RepoInMemoryHolder
import com.lenta.bp14.requests.check_price.CheckPriceTaskInfoNetRequest
import com.lenta.bp14.requests.check_price.ICheckPriceTaskInfoNetRequest
import com.lenta.bp14.requests.tasks.*
import com.lenta.bp14.requests.work_list.IWorkListTaskInfoNetRequest
import com.lenta.bp14.requests.work_list.WorkListTaskInfoNetRequest
import com.lenta.shared.di.AppScope
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule.Declarations::class])
class AppModule {

    @Module
    internal interface Declarations {
        @Binds
        @AppScope
        fun bindAppUpdateInstaller(realisation: AppUpdaterInstallerFromFmp): AppUpdateInstaller

        @Binds
        @AppScope
        fun bindGeneralTaskManager(realisation: GeneralTaskManager): IGeneralTaskManager

        @Binds
        @AppScope
        fun bindRepoInMemoryHolder(realisation: RepoInMemoryHolder): IRepoInMemoryHolder

        @Binds
        @AppScope
        fun bindScreenNavigator(realisation: ScreenNavigator): IScreenNavigator

        @Binds
        @AppScope
        fun bindGeneralRepo(realisation: GeneralRepo): IGeneralRepo

        @Binds
        @AppScope
        fun bindVibrateHelper(realisation: VibrateHelper): IVibrateHelper

        @Binds
        @AppScope
        fun bindPriceInfoParser(realisation: PriceInfoParser): IPriceInfoParser

        @Binds
        @AppScope
        fun bindITaskListFilteredNetRequest(realisation: TaskListFilteredNetRequest): ITaskListFilteredNetRequest

        @Binds
        @AppScope
        fun bindITaskListUpdateNetRequest(realisation: TaskListUpdateNetRequest): ITaskListUpdateNetRequest

        @Binds
        @AppScope
        fun bindITaskSearchHelper(realisation: TasksSearchHelper): ITasksSearchHelper

        @Binds
        @AppScope
        fun bindICheckPriceTaskInfoNetRequest(realisation: CheckPriceTaskInfoNetRequest): ICheckPriceTaskInfoNetRequest

        @Binds
        @AppScope
        fun bindIPrintTask(realisation: PrintTask): IPrintTask

        @Binds
        @AppScope
        fun bindIUnlockTaskNetRequest(realisation: UnlockTaskNetRequest): IUnlockTaskNetRequest

        @Binds
        @AppScope
        fun bindWorkListTaskInfoNetRequest(realisation: WorkListTaskInfoNetRequest): IWorkListTaskInfoNetRequest

        @Binds
        @AppScope
        fun bindBigDataMaxPrint(realisation: BigDatamaxPrintImpl): BigDatamaxPrint

        @Binds
        @AppScope
        fun bindPersistTaskData(realisation: PersistTaskData): IPersistTaskData

        @Binds
        @AppScope
        fun bindResourceManager(realisation: ResourceManager): IResourceManager

    }

    @Provides
    @AppScope
    internal fun provideAppUpdaterConfig(): AppUpdaterConfig {
        return AppUpdaterConfig(folderName = "bp14", applicationId = APPLICATION_ID)
    }

}