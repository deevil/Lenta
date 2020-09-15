package com.lenta.bp16.di

import app_update.AppUpdateInstaller
import app_update.AppUpdaterConfig
import app_update.AppUpdaterInstallerFromFmp
import com.lenta.bp16.BuildConfig.APPLICATION_ID
import com.lenta.bp16.data.IPrinter
import com.lenta.bp16.data.IScales
import com.lenta.bp16.data.Printer
import com.lenta.bp16.data.Scales
import com.lenta.bp16.model.*
import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.bp16.model.data_storage.IngredientDataPersistStorage
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.model.warehouse.WarehousePersistStorage
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.navigation.ScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.platform.resource.ResourceManager
import com.lenta.bp16.repository.*
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
        fun bindRepoInMemoryHolder(realisation: RepoInMemoryHolder): IRepoInMemoryHolder

        @Binds
        @AppScope
        fun bindScreenNavigator(realisation: ScreenNavigator): IScreenNavigator

        @Binds
        @AppScope
        fun bindGeneralRepository(realisation: DatabaseRepository): IDatabaseRepository

        @Binds
        @AppScope
        fun bindIngredientsRepository(realisation: IngredientsRepository): IIngredientsRepository

        @Binds
        @AppScope
        fun bindMovementRepository(realisation: MovementRepository): IMovementRepository

        @Binds
        @AppScope
        fun bindTaskManager(realisation: TaskManager): ITaskManager

        @Binds
        @AppScope
        fun bindResourceManager(realisation: ResourceManager): IResourceManager

        @Binds
        @AppScope
        fun bindAttributeManager(realisation: AttributeManager): IAttributeManager

        @Binds
        @AppScope
        fun bindScales(realisation: Scales): IScales

        @Binds
        @AppScope
        fun bindPrinter(realisation: Printer): IPrinter

        @Binds
        @AppScope
        fun bindPersistTaskData(realisation: PersistLabelList): IPersistLabelList

        @Binds
        @AppScope
        fun bindPersistWarehouseData(realisation: WarehousePersistStorage): IWarehousePersistStorage

        @Binds
        @AppScope
        fun bindPersistIngredientData(realisation: IngredientDataPersistStorage): IIngredientDataPersistStorage

    }

    @Provides
    @AppScope
    internal fun provideAppUpdaterConfig(): AppUpdaterConfig {
        return AppUpdaterConfig(folderName = "bp16", applicationId = APPLICATION_ID)
    }

}