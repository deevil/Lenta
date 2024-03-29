package com.lenta.bp14.main

import android.Manifest
import androidx.lifecycle.ViewModelProvider
import com.lenta.bp14.di.AppComponent
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.di.FromParentToCoreProvider
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.lenta.shared.platform.fragment.CoreFragment

class MainActivity : CoreMainActivity() {

    private var mainViewModel: MainViewModel? = null
    private val numberScreenGenerator = NumberScreenGenerator()

    val appComponent: AppComponent by lazy {
        getAppComponent(coreComponent)
    }

    override fun getViewModel(): MainViewModel {
        appComponent.let { component ->
            component.inject(this)
            foregroundActivityProvider.setActivity(this)
            return ViewModelProvider(this).get(MainViewModel::class.java).apply {
                mainViewModel = this
                component.inject(this)
            }
        }
    }

    override fun onClickExit() {
        mainViewModel?.onExitClick()
    }

    override fun onPause() {
        super.onPause()
        /*startActivity(Intent(applicationContext, this::class.java).apply {
            flags = FLAG_ACTIVITY_REORDER_TO_FRONT
        })*/
    }

    override fun generateNumberScreenFromPostfix(postfix: String?): String? {
        return numberScreenGenerator.generateNumberScreenFromPostfix(postfix)
    }

    override fun generateNumberScreen(fragment: CoreFragment<*, *>): String? {
        return numberScreenGenerator.generateNumberScreen(fragment)
    }

    override fun getPrefixScreen(fragment: CoreFragment<*, *>): String {
        return numberScreenGenerator.getPrefixScreen(fragment)
    }

    override fun getAdditionalListOfRequiredPermissions(): List<String> {
        return listOf(Manifest.permission.CAMERA)
    }

    override fun provideFromParentToCoreProvider(): FromParentToCoreProvider? {
        return getAppComponent(coreComponent)
    }

}
