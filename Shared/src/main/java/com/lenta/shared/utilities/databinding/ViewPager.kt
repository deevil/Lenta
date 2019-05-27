package com.lenta.shared.utilities.databinding

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.implementationOf


@BindingAdapter(value = ["viewPagerSettings", "tabPosition", "pageSelectionListener"], requireAll = false)
fun setupViewPager(viewPager: ViewPager,
                   viewPagerSettings: ViewPagerSettings?,
                   tabPosition: Int?,
                   pageSelectionListener: PageSelectionListener?) {

    Logg.d { "setupViewPager tabPosition: $tabPosition" }

    if (viewPagerSettings == null) {
        return
    }

    if (viewPager.adapter == null) {
        viewPager.let { vp ->
            vp.adapter = ViewPagerAdapter(viewPagerSettings)
            vp.offscreenPageLimit = viewPagerSettings.countTab()
            vp.currentItem = tabPosition ?: 0
            tabPosition?.let {
                vp.setCurrentItem(it, false)
            }

        }

        pageSelectionListener?.let {

            pageSelectionListener.onPageSelected(viewPager.currentItem)

            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    viewPager
                            .tag
                            .implementationOf(PageSelectionListener::class.java)
                            ?.onPageSelected(position)
                }

            })
        }
    }

    viewPager.tag = pageSelectionListener

    if (tabPosition != null && viewPager.currentItem != tabPosition) {
        viewPager.setCurrentItem(tabPosition, false)
    }


}

interface ViewPagerSettings {

    fun getPagerItemView(container: ViewGroup, position: Int): View

    fun getTextTitle(position: Int): String

    fun countTab(): Int

}

interface PageSelectionListener {
    fun onPageSelected(position: Int)
}

internal class ViewPagerAdapter(val viewPagerSettings: ViewPagerSettings) : PagerAdapter() {

    override fun getCount(): Int = viewPagerSettings.countTab()

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = viewPagerSettings.getPagerItemView(container, position)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return viewPagerSettings.getTextTitle(position)
    }
}