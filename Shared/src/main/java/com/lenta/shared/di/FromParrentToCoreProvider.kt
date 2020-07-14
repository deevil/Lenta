package com.lenta.shared.di

import app_update.AppUpdateInstaller

interface FromParentToCoreProvider {
    fun getAppUpdateInstaller(): AppUpdateInstaller
}