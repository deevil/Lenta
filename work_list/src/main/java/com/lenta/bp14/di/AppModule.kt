package com.lenta.bp14.di

import com.lenta.bp14.models.GeneralTaskManager
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_list.ICheckListTask
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.check_price.PriceInfoParser
import com.lenta.bp14.models.general.GeneralRepo
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.not_exposed_products.INotExposedProductsTask
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskManager
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
import com.lenta.bp14.requests.check_price.CheckPriceNetRequest
import com.lenta.bp14.requests.check_price.ICheckPriceNetRequest
import com.lenta.bp14.requests.not_exposed_product.IProductInfoForNotExposedNetRequest
import com.lenta.bp14.requests.not_exposed_product.ProductInfoForNotExposedNetRequest
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
        fun bindGeneralTaskManager(realisation: GeneralTaskManager): IGeneralTaskManager

        @Binds
        @AppScope
        fun bindRepoInMemoryHolder(realisation: RepoInMemoryHolder): IRepoInMemoryHolder

        @Binds
        @AppScope
        fun bindScreenNavigator(realisation: ScreenNavigator): IScreenNavigator

        @Binds
        @AppScope
        fun bindCheckPriceRequest(realisation: CheckPriceNetRequest): ICheckPriceNetRequest

        @Binds
        @AppScope
        fun bindProductInfoForNotExposedNetRequest(realisation: ProductInfoForNotExposedNetRequest): IProductInfoForNotExposedNetRequest

        @Binds
        @AppScope
        fun bindGeneralRepo(realisation: GeneralRepo): IGeneralRepo

        @Binds
        @AppScope
        fun bindSoundPlayer(realisation: SoundPlayer): ISoundPlayer

        @Binds
        @AppScope
        fun bindVibrateHelper(realisation: VibrateHelper): IVibrateHelper

        @Binds
        @AppScope
        fun bindPriceInfoParser(realisation: PriceInfoParser): IPriceInfoParser


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
    internal fun provideNotExposedProductsTask(taskManager: NotExposedProductsTaskManager): INotExposedProductsTask {
        return taskManager.getTask()!!
    }

}