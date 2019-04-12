package com.lenta.bp10.platform

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.di.getAppComponent
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.main_activity.BaseMainActivity
import com.lenta.shared.platform.main_activity.MainViewModel
import com.lenta.shared.platform.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logger
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class MainActivity : BaseMainActivity() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider

    override fun injectThis() {
        getAppComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.d { "version: ${hyperHive.stateAPI.versionPlugin}" }
        Logger.d { "hyperHive hash: ${hyperHive.hashCode()}" }
        Logger.d { "getAppComponent hash: ${getAppComponent().hashCode()}" }
        Logger.d { "foregroundActivityProvider hash: ${foregroundActivityProvider.hashCode()}" }

        binding?.vm = ViewModelProviders.of(this).get(MainViewModel::class.java)

        binding?.layoutTopToolbar?.bExit?.setOnClickListener {
            binding?.vm?.topToolbarUiModel?.value = TopToolbarUiModel(title = System.currentTimeMillis().toString(), description = "Карточка задания")
        }

    }

    override fun onResume() {
        super.onResume()
        foregroundActivityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        foregroundActivityProvider.clear()
    }

}
