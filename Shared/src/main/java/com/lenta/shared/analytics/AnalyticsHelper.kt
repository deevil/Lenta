package com.lenta.shared.analytics

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.view.View
import android.widget.TextView
import com.lenta.shared.R
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.network_state.NetworkInfo
import com.lenta.shared.utilities.extentions.implementationOf
import com.mobrun.plugin.models.BaseStatus

class AnalyticsHelper(
        private val analytics: IAnalytics,
        private val context: Context) {

    fun onPermissionGranted() {
        analytics.enableLogs(true)
    }


    fun logAppInfo() {
        analytics.logTrace(message = context.packageManager?.getPackageInfo(context.packageName, 0)?.let { packageInfo ->
            "app: ${packageInfo.packageName}, v:${packageInfo.versionName}"
        } ?: "")
    }

    @SuppressLint("HardwareIds")
    fun logDeviceInfo() {
        analytics.logTrace(message = "DeviceInfo. API:  ${android.os.Build.VERSION.SDK_INT}")
        analytics.logTrace(message = "DeviceInfo. Device:  ${android.os.Build.DEVICE}")
        analytics.logTrace(message = "DeviceInfo. MODEL:  ${android.os.Build.MODEL}")
        analytics.logTrace(message = "DeviceInfo. PRODUCT:  ${android.os.Build.PRODUCT}")
        analytics.logTrace(message = "DeviceInfo. DeviceID:  ${Settings.Secure.getString(context.contentResolver, "android_id")}")
    }

    fun onClickToolbarButton(view: View) {
        analytics.logTrace(message = getToolbarsButtonInfo(view))
    }

    fun onNewScreen(fragment: CoreFragment<*, *>?) {
        fragment?.let {
            analytics.logTrace(message = "экран: ${fragment.getPageNumber()
                    ?: ""} (${fragment.javaClass.simpleName})")
        }
    }


    private fun getToolbarsButtonInfo(view: View): String {
        return "${when (view.id) {
            R.id.b_1 -> "нижняя кнопка 1"
            R.id.b_2 -> "нижняя кнопка 2"
            R.id.b_3 -> "нижняя кнопка 3"
            R.id.b_4 -> "нижняя кнопка 4"
            R.id.b_5 -> "нижняя кнопка 5"
            R.id.b_topbar_1 -> "верхняя кнопка 1"
            R.id.b_topbar_2 -> "верхняя кнопка 2"
            else -> "неизвестная кнопка"
        }} - \"${view.implementationOf(TextView::class.java)?.text ?: ""}\""
    }

    fun logNetworkInfo(networkInfo: NetworkInfo?) {
        analytics.logTrace(message = "Состояние сети: $networkInfo")
    }

    fun logRequestError(nameResource: String?, baseStatus: BaseStatus) {
        analytics.logTrace(message = "Ошибка FMP запроса. Ресурс: $nameResource. статус:  $baseStatus")
    }

    fun infoScreenMessage(message: String) {
        analytics.logTrace(message = "Сообщение пользователю: $message")
    }

    fun onStartFmpRequest(resourceName: String, params: String? = null) {
        analytics.logTrace(message = "-> $resourceName${if (params == null) "" else ", params: $params"}")
    }

    fun onFinishFmpRequest(resourceName: String?) {
        analytics.logTrace(message = "<- $resourceName")
    }

    fun onRetCodeNotEmpty(status: String) {
        analytics.logTrace(message = "Параметр экспорта EV_RETCODE «не равно» 0. $status")
    }


}