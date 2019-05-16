package com.lenta.shared.utilities.databinding

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.lenta.shared.utilities.extentions.implementationOf


@BindingAdapter(value = ["viewPagerSettings", "position", "pageSelectionListener"], requireAll = false)
fun setupViewPager(viewPager: ViewPager,
                   viewPagerSettings: ViewPagerSettings?,
                   position: Int?,
                   pageSelectionListener: PageSelectionListener?) {

    if (viewPagerSettings == null) {
        return
    }

    if (viewPager.adapter == null) {
        viewPager.let {
            it.adapter = ViewPagerAdapter(viewPagerSettings)
            it.offscreenPageLimit = viewPagerSettings.countTab()
            it.currentItem = position ?: 0
        }
    }

    viewPager.tag = pageSelectionListener

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