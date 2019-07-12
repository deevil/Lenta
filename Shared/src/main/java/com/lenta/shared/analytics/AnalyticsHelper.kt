package com.lenta.shared.analytics

import android.view.View
import android.widget.TextView
import com.lenta.shared.R
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf
import javax.inject.Inject

class AnalyticsHelper @Inject constructor(private val analytics: IAnalytics) {
    fun onClickToolbarButton(view: View) {
        analytics.logTrace(message = getToolbarsButtonInfo(view))
    }

    fun onNewScreen(fragment: CoreFragment<*, *>?) {
        fragment?.let {
            analytics.logTrace(message = "экран: ${fragment.getPageNumber()?:""} (${fragment.javaClass.simpleName})")
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

}