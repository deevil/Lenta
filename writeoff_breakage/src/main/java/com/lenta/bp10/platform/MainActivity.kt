package com.lenta.bp10.platform

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.R
import com.lenta.bp10.databinding.ActivityMainBinding
import com.lenta.bp10.di.getAppComponent
import com.lenta.bp10.platform.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logger
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getLayoutId() = R.layout.activity_main

    @Inject
    lateinit var hyperHive: HyperHive

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getAppComponent().inject(this)

        Logger.d { "version: ${hyperHive.stateAPI.versionPlugin}" }
        Logger.d { "hyperHive hash: ${hyperHive.hashCode()}" }
        Logger.d { "getAppComponent hash: ${getAppComponent().hashCode()}" }

        binding?.vm = ViewModelProviders.of(this).get(MainViewModel::class.java)

        //binding?.vm?.topToolbarUiModel?.value = TopToolbarUiModel(title = "ТК - 0002", description = "Карточка задания")

        binding?.layoutTopToolbar?.bExit?.setOnClickListener {
            binding?.vm?.topToolbarUiModel?.value = TopToolbarUiModel(title = System.currentTimeMillis().toString(), description = "Карточка задания")
        }

    }

}
