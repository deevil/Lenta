package com.lenta.bp14.di

import com.lenta.bp14.models.check_list.CheckListTaskDescription
import dagger.*
import javax.inject.Scope

@CheckListScope
@Component(modules = [CheckListModule::class], dependencies = [AppComponent::class])

interface CheckListComponent {
}


@Module(includes = [CheckListModule.Declarations::class])
class CheckListModule(private val taskDescription: CheckListTaskDescription) {

    @Module
    internal interface Declarations {

    }

}

@Scope
@Retention
annotation class CheckListScope